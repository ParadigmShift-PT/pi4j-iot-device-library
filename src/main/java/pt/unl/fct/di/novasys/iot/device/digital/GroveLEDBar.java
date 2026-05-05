package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

/**
 * Driver for the Grove LED Bar (MY9221-driven, 10 LEDs of variable
 * brightness). Communicates over a two-wire bit-banged protocol on
 * {@code line} (clock) and {@code line + 1} (data). Supports
 * level-meter, per-LED, bitmask, and toggle-style output.
 *
 * <p>The bar is colour-graded green-to-red along its length; the
 * {@code reverse} flag flips this orientation so brighter levels light
 * up from the red end instead of the green one.
 */
public class GroveLEDBar extends DigitalOutputDevice {
    private static final int LED_MAX_COUNT = 24;
    private static final int LED_COUNT = 10;
    private boolean reverse;
    private int showCount;
    private int[] led;

    private static final int LED_TURN_OFF = 0;
    private static final int LED_FULL_BRIGHTNESS = 0xFF;

    /**
     * Constructs a Grove LED Bar.
     *
     * @param pi4j    Pi4J context
     * @param name    human-readable name
     * @param line    clock pin (data pin is {@code line + 1})
     * @param ID      caller-assigned device identifier
     * @param reverse {@code true} to render levels from the red end
     */
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

    /**
     * Lights the bar as a level meter from 0 to {@code showCount}. The
     * fractional part fades the highest lit LED (8 brightness steps).
     *
     * @param level fill level, clamped to [0, showCount]
     */
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

    /**
     * Sets one LED to a specific brightness, leaving the rest unchanged.
     *
     * @param ledNum     1-based LED index (clamped to {@code showCount})
     * @param brightness brightness, 0.0 – 1.0 (8 effective steps)
     */
    public void setLED(int ledNum, float brightness) {
        ledNum = Math.max(1, Math.min(ledNum, showCount));
        brightness = Math.max(0.0f, Math.min(brightness, 1.0f));

        led[ledNum - 1] = ~(~0 << ((int)(brightness * 8) & 0xff));
        send();
    }

    /**
     * Toggles one LED between off and full brightness.
     *
     * @param ledNum 1-based LED index (clamped to {@code showCount})
     */
    public void toggleLED(int ledNum) {
        int i = ledNum <= showCount ? ledNum - 1 : showCount - 1;

        led[i] = led[i] != 0 ? LED_TURN_OFF : LED_FULL_BRIGHTNESS;
        send();
    }

    /**
     * Sets the bar from a bitmask, one bit per LED. Bit 0 controls LED 1,
     * bit 1 controls LED 2, and so on; lit bits go to full brightness.
     *
     * @param value bitmask
     */
    public void setBits(int value) {
        for (int i = 0; i < LED_COUNT; i++, value >>= 1) {
            led[i] = (value & 1) != 0 ? LED_FULL_BRIGHTNESS : LED_TURN_OFF;
        }

        send();
    }

    /**
     * Sets the number of LEDs participating in subsequent operations.
     * LEDs beyond {@code count} are turned off.
     *
     * @param count number of active LEDs
     */
    public void setLEDNum(int count) {
        showCount = count;

        for (int i = count; i < LED_COUNT; i++) {
            led[i] = LED_TURN_OFF;
        }
    }

    /**
     * Sets the bar's fill direction.
     *
     * @param reverse {@code true} to fill from the red end, {@code false}
     *                to fill from the green end
     */
    public void setGreenToRed(boolean reverse) {
        this.reverse = reverse;
        send();
    }

    /**
     * @return a bitmask of which LEDs are currently lit (any non-zero
     *         brightness counts as lit)
     */
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
