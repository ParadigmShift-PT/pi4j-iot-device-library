package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class Grove4DigitDisplay extends DigitalOutputDevice {
    private static final byte ADDR_AUTO = 0x40;
    private static final byte ADDR_FIXED = 0x44;
    public static final byte BRIGHT_DARKEST = 0;
    public static final byte BRIGHT_TYPICAL = 2;
    public static final byte BRIGHTEST = 7;
    public static final short DIGITS = 4;

    private static byte tube_tab[] = {
        0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07,
        0x7f, 0x6f, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71}; // 0~9,A,b,C,d,E,F

    private int cmd_set_data;
    private int cmd_set_addr;
    private int cmd_disp_ctrl;

    private boolean pointFlag;

    public Grove4DigitDisplay(Context pi4j, String name, int line, int ID) {
        super(pi4j,
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name + "_clk")
                  .name(name + "_clk — " + ID)
                  .address(line)
                  .initial(DigitalState.LOW)
                  .build(),
              DigitalInput.newConfigBuilder(pi4j)
                  .id(name + "_in")
                  .name(name + " — " + ID)
                  .address(line + 1)
                  .build(),
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name + "_out")
                  .name(name + " — " + ID)
                  .address(line + 1)
                  .initial(DigitalState.LOW)
                  .build(),
              ID);

        this.pointFlag = true;

        set(BRIGHT_TYPICAL, 0x40, 0xc0);
        clearDisplay();
    }

    private int writeByte(byte b) {
        for (int i = 0; i < 8; i++) {
            clk.state(DigitalState.LOW);

            if ((b & 0x01) == 1) {
                dataOut.state(DigitalState.HIGH);
            } else {
                dataOut.state(DigitalState.LOW);
            }

            b >>= 1;
            clk.state(DigitalState.HIGH);
        }

        clk.state(DigitalState.LOW); // wait for ACK
        dataOut.state(DigitalState.HIGH);
        clk.state(DigitalState.HIGH);

        setDataPinInput();

        delayMicroseconds(50);
        int ack = dataIn.isLow() ? 0 : 1;

        setDataPinOutput();

        delayMicroseconds(50);
        if (ack == 0) {
            dataOut.state(DigitalState.LOW);
        }

        delayMicroseconds(100);

        return ack;
    }

    public void display(byte[] disp_data) {
        byte[] seg_data = coding(disp_data);

        start();
        writeByte(ADDR_AUTO);
        stop();

        start();
        writeByte((byte)cmd_set_addr);

        for (int i = 0; i < DIGITS; i++) {
            writeByte((byte)seg_data[i]);
        }
        stop();

        start();
        writeByte((byte)unsignedByte(cmd_disp_ctrl));
        stop();
    }

    public void display(int bit_addr, byte disp_data) {
        byte seg_data = coding(disp_data);

        start();
        writeByte(ADDR_FIXED);
        stop();

        start();
        writeByte((byte)(unsignedByte(bit_addr) | 0xc0));
        writeByte(seg_data);
        stop();

        start();
        writeByte((byte)cmd_disp_ctrl);
        stop();
    }

    public void displayNum(float num, int decimal, boolean minus) {
        int number = (int)Math.round(Math.abs(num) * Math.pow(10, decimal));

        if (decimal == 2) {
            point(true);
        } else {
            point(false);
        }

        for (int i = 0; i < DIGITS - (minus && num < 0 ? 1 : 0); ++i) {
            int j = DIGITS - i - 1;

            if (number != 0) {
                display(unsignedByte(j), (byte)(number % 10));
            } else {
                display(unsignedByte(j), (byte)0x7f); // display nothing
            }

            number /= 10;
        }

        if (minus && num < 0) {
            display(0, (byte)'-'); // display '-'
        }
    }

    void displayStr(String str, int loop_delay) throws InterruptedException {
        int end = str.length();
        if (end <= DIGITS) {
            for (int i = 0; i < DIGITS; i++) {
                if (i < 0 ||
                    i >= end) { // display nothing on the remaining display
                    display(i, (byte)0x7f);
                } else {
                    display(i, (byte)str.charAt(i));
                }
            }
        } else {
            int offset = -DIGITS;

            for (int i = 0; i <= end + DIGITS; i++) {
                for (int j = offset, k = 0; j < DIGITS + offset; j++, k++) {
                    if (j < 0 || j >= end) {
                        display(k, (byte)0x7f);
                    } else {
                        display(k, (byte)str.charAt(j));
                    }
                }
                offset++;
                Thread.sleep(loop_delay); // loop delay
            }
        }
    }

    public void point(boolean point) { this.pointFlag = point; }

    private byte[] coding(byte[] disp_data) {
        for (int i = 0; i < DIGITS; i++) {
            disp_data[i] = coding(disp_data[i]);
        }

        return disp_data;
    }

    private byte coding(byte disp_data) {
        if (disp_data == 0x7f) {
            disp_data = 0x00; // clear digit
        } else if (disp_data >= 0 && disp_data < tube_tab.length) {
            disp_data = tube_tab[disp_data];
        } else if (disp_data >= '0' && disp_data <= '9') {
            disp_data = tube_tab[(int)disp_data - 48];
        } else {
            disp_data = (byte)unsignedByte(char2segments((char)disp_data));
        }
        disp_data += pointFlag ? 0x80 : 0;

        return disp_data;
    }

    // @formatter:off
    private int char2segments(char c) {
        switch (c) {
        case '_': return 0x08;
        case '^': return 0x01;  // ¯
        case '-': return 0x40;
        case '*': return 0x63;  // °
        case ' ': return 0x00;  // space
        case 'A': return 0x77;  // upper case A
        case 'a': return 0x5f;  // lower case a
        case 'B':               // lower case b
        case 'b': return 0x7c;  // lower case b
        case 'C': return 0x39;  // upper case C
        case 'c': return 0x58;  // lower case c
        case 'D':               // lower case d
        case 'd': return 0x5e;  // lower case d
        case 'E':               // upper case E
        case 'e': return 0x79;  // upper case E
        case 'F':               // upper case F
        case 'f': return 0x71;  // upper case F
        case 'G':               // upper case G
        case 'g': return 0x35;  // upper case G
        case 'H': return 0x76;  // upper case H
        case 'h': return 0x74;  // lower case h
        case 'I': return 0x06;  // 1
        case 'i': return 0x04;  // lower case i
        case 'J': return 0x1e;  // upper case J
        case 'j': return 0x16;  // lower case j
        case 'K':               // upper case K
        case 'k': return 0x75;  // upper case K
        case 'L':               // upper case L
        case 'l': return 0x38;  // upper case L
        case 'M':               // twice tall n
        case 'm': return 0x37;  // twice tall ∩
        case 'N':               // lower case n
        case 'n': return 0x54;  // lower case n
        case 'O':               // lower case o
        case 'o': return 0x5c;  // lower case o
        case 'P':               // upper case P
        case 'p': return 0x73;  // upper case P
        case 'Q': return 0x7b;  // upper case Q
        case 'q': return 0x67;  // lower case q
        case 'R':               // lower case r
        case 'r': return 0x50;  // lower case r
        case 'S':               // 5
        case 's': return 0x6d;  // 5
        case 'T':               // lower case t
        case 't': return 0x78;  // lower case t
        case 'U':               // lower case u
        case 'u': return 0x1c;  // lower case u
        case 'V':               // twice tall u
        case 'v': return 0x3e;  // twice tall u
        case 'W': return 0x7e;  // upside down A
        case 'w': return 0x2a;  // separated w
        case 'X':               // upper case H
        case 'x': return 0x76;  // upper case H
        case 'Y':               // lower case y
        case 'y': return 0x6e;  // lower case y
        case 'Z':               // separated Z
        case 'z': return 0x1b;  // separated Z
        }
        return 0;
    }
    // @formatter:on

    public void clearDisplay() {
        display(0x00, (byte)0x7f);
        display(0x01, (byte)0x7f);
        display(0x02, (byte)0x7f);
        display(0x03, (byte)0x7f);
    }

    public void start() {
        dataOut.state(DigitalState.HIGH);
        clk.state(DigitalState.HIGH);
        dataOut.state(DigitalState.LOW);
        clk.state(DigitalState.LOW);
    }

    public void stop() {
        dataOut.state(DigitalState.LOW);
        clk.state(DigitalState.LOW);
        dataOut.state(DigitalState.HIGH);
        clk.state(DigitalState.HIGH);
    }

    public void set(int brightness, int set_data, int set_addr) {
        cmd_set_data = set_data;
        cmd_set_addr = set_addr;
        cmd_disp_ctrl = unsignedByte(brightness) + 0x88;
    }

    private int unsignedByte(byte b) { return b & 0xFF; }

    private int unsignedByte(short b) { return b & 0xFF; }

    private int unsignedByte(int b) { return b & 0xFF; }
}
