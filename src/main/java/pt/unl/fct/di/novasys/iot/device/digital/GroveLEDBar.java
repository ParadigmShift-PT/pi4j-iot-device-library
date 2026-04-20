package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class GroveLEDBar extends DigitalOutputDevice {
    private static final int LED_MAX_COUNT = 24;
    private static final int LED_COUNT = 10;
    private boolean reverse;
    private int showCount;
    private int[] led;

    private static final int LED_TURN_OFF = 0;
    private static final int LED_FULL_BRIGHTNESS = 0xFF;

    public GroveLEDBar(Context pi4j, String name, int line, int ID,
                       boolean reverse) {
        super(pi4j,
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name + "_clk")
                  .name(name + "_clk — " + ID)
                  .address(line)
                  .initial(DigitalState.LOW)
                  .build(),
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name)
                  .name(name + " — " + ID)
                  .address(line + 1)
                  .initial(DigitalState.LOW)
                  .build(),
              ID);

        this.reverse = reverse;
        this.showCount = LED_COUNT;

        this.led = new int[LED_MAX_COUNT];
        for (int i = 0; i < LED_MAX_COUNT; i++) {
            led[i] = LED_TURN_OFF;
        }
    }

    private void send(int bits) {
        bits &= 0xFFFF;

        boolean clkState = false;
        for (int i = 0; i < 16; i++) {
            dataOut.state((bits & 0x8000) != 0 ? DigitalState.HIGH
                                               : DigitalState.LOW);
            clk.state(clkState ? DigitalState.HIGH : DigitalState.LOW);
            clkState = !clkState;
            bits <<= 1;
        }
    }

    private void send() {
        if (reverse) {
            send(0x00); // send cmd(0x00)

            for (int i = showCount; i-- > 0;) {
                send(led[i]);
            }

            for (int i = 0; i < 12 - showCount; i++) {
                send(0x00);
            }
        } else {
            send(0x00); // send cmd(0x00)

            for (int i = 0; i < 12; i++) {
                send(led[i]);
            }
        }

        latch();
    }

    private void latch() {
        dataOut.state(DigitalState.LOW);
        clk.state(DigitalState.HIGH); clk.state(DigitalState.LOW);
        clk.state(DigitalState.HIGH); clk.state(DigitalState.LOW);
        delayMicroseconds(240);

        dataOut.state(DigitalState.HIGH); dataOut.state(DigitalState.LOW);
        dataOut.state(DigitalState.HIGH); dataOut.state(DigitalState.LOW);
        dataOut.state(DigitalState.HIGH); dataOut.state(DigitalState.LOW);
        dataOut.state(DigitalState.HIGH); dataOut.state(DigitalState.LOW);
        delayMicroseconds(1);

        clk.state(DigitalState.HIGH);
        clk.state(DigitalState.LOW);
    }

    public void setLevel(float level) {
        level = Math.max(0.0f, Math.min(level, showCount));
        level *= 8;

        for (int i = 0; i < showCount; i++) {
            led[i] = (level > 8) ? ~0 : (level > 0) ? ~(~0 << (byte)level) : 0;
            level -= 8;
        }

        for (int i = showCount; i < LED_COUNT; i++) {
            led[i] = LED_TURN_OFF;
        }
        send();
    }

    public void setLED(int ledNum, float brightness) {
        ledNum = Math.max(1, Math.min(ledNum, showCount));
        brightness = Math.max(0.0f, Math.min(brightness, 1.0f));

        led[ledNum - 1] = ~(~0 << ((int)(brightness * 8) & 0xff));
        send();
    }

    public void toggleLED(int ledNum) {
        int i = ledNum <= showCount ? ledNum - 1 : showCount - 1;

        led[i] = led[i] != 0 ? LED_TURN_OFF : LED_FULL_BRIGHTNESS;
        send();
    }

    public void setBits(int value) {
        for (int i = 0; i < LED_COUNT; i++, value >>= 1) {
            led[i] = (value & 1) != 0 ? LED_FULL_BRIGHTNESS : LED_TURN_OFF;
        }

        send();
    }

    public void setLEDNum(int count) {
        showCount = count;

        for (int i = count; i < LED_COUNT; i++) {
            led[i] = LED_TURN_OFF;
        }
    }

    public void setGreenToRed(boolean reverse) {
        this.reverse = reverse;
        send();
    }

    public int getBits() {
        int val = 0;

        for (int i = 0; i < showCount; i++) {
            if (led[i] != LED_TURN_OFF) {
                val |= 1 << i;
            }
        }

        return val;
    }
}
