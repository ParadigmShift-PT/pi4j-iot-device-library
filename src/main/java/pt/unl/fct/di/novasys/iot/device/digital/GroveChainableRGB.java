package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class GroveChainableRGB extends DigitalOutputDevice {
    private final static int RED = 0;
    private final static int GREEN = 1;
    private final static int BLUE = 2;
    private final static int PULSE_DELAY = 20; // microseconds

    private int numLeds;

    private byte[] ledState;

    public GroveChainableRGB(Context pi4j, String name, int line, int ID,
                             int numLeds) {
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

        this.numLeds = numLeds;
        this.ledState = new byte[numLeds * 3];

        for (byte i = 0; i < numLeds; i++) {
            setColorRGB(i, (byte)0, (byte)0, (byte)0);
        }
    }

    public void clk() {
        clk.state(DigitalState.LOW);
        delayMicroseconds(PULSE_DELAY);
        clk.state(DigitalState.HIGH);
        delayMicroseconds(PULSE_DELAY);
    }

    private void sendByte(byte b) {
        for (byte i = 0; i < 8; i++) {
            if ((b & 0x80) != 0) {
                dataOut.state(DigitalState.HIGH);
            } else {
                dataOut.state(DigitalState.LOW);
            }
            clk();

            b <<= 1;
        }
    }

    private void sendColor(byte red, byte green, byte blue) {
        short prefix = 0b11000000;
        if ((blue & 0x80) == 0) {
            prefix |= 0b00100000;
        }
        if ((blue & 0x40) == 0) {
            prefix |= 0b00010000;
        }
        if ((green & 0x80) == 0) {
            prefix |= 0b00001000;
        }
        if ((green & 0x40) == 0) {
            prefix |= 0b00000100;
        }
        if ((red & 0x80) == 0) {
            prefix |= 0b00000010;
        }
        if ((red & 0x40) == 0) {
            prefix |= 0b00000001;
        }
        sendByte((byte)prefix);

        sendByte(blue);
        sendByte(green);
        sendByte(red);
    }

    public void setColorRGB(byte led, byte red, byte green, byte blue) {
        // send data frame prefix (32x "0")
        sendByte((byte)0x00);
        sendByte((byte)0x00);
        sendByte((byte)0x00);
        sendByte((byte)0x00);

        // send color data for each one of the leds
        for (byte i = 0; i < numLeds; i++) {
            if (i == led) {
                ledState[i * 3 + RED] = red;
                ledState[i * 3 + GREEN] = green;
                ledState[i * 3 + BLUE] = blue;
            }

            sendColor(ledState[i * 3 + RED], ledState[i * 3 + GREEN],
                      ledState[i * 3 + BLUE]);
        }

        // terminate data frame (32x "0")
        sendByte((byte)0x00);
        sendByte((byte)0x00);
        sendByte((byte)0x00);
        sendByte((byte)0x00);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public void setColorHSB(byte led, float hue, float saturation,
                            float brightness) {
        float r, g, b;

        clamp(hue, 0.0f, 1.0f);
        clamp(saturation, 0.0f, 1.0f);
        clamp(brightness, 0.0f, 1.0f);

        if (saturation == 0.0) {
            r = g = b = brightness;
        } else {
            float q = brightness < 0.5f
                          ? brightness * (1.0f + saturation)
                          : brightness + saturation - brightness * saturation;
            float p = 2.0f * brightness - q;
            r = hue2rgb(p, q, hue + 1.0f / 3.0f);
            g = hue2rgb(p, q, hue);
            b = hue2rgb(p, q, hue - 1.0f / 3.0f);
        }

        setColorRGB(led, (byte)(255.0 * r), (byte)(255.0 * g),
                    (byte)(255.0 * b));
    }

    private float hue2rgb(float p, float q, float t) {
        if (t < 0.0) {
            t += 1.0;
        }
        if (t > 1.0) {
            t -= 1.0;
        }
        if (t < 1.0 / 6.0) {
            return p + (q - p) * 6.0f * t;
        }
        if (t < 1.0 / 2.0) {
            return q;
        }
        if (t < 2.0 / 3.0) {
            return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        }

        return p;
    }

    public int getNumLeds() { return numLeds; }
}
