package pt.unl.fct.di.novasys.iot.device.i2c;

import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import java.io.IOException;
import pt.unl.fct.di.novasys.iot.device.I2CDevice;

/**
 * Driver for the Grove 8-Channel I²C Hub (TCA9548A). Multiplexes one
 * upstream I²C bus across eight downstream channels, each of which can
 * be independently opened or closed. This is essential when several
 * downstream devices share the same I²C address (e.g., two identical
 * sensors) and would otherwise collide on bus 1.
 *
 * <p>The hub itself sits at I²C address {@code 0x70}. Use
 * {@link #openChannel(TCAChannel)} / {@link #closeChannel(TCAChannel)}
 * to gate access to each downstream device, and {@link #openAll()} /
 * {@link #closeAll()} to enable or isolate every channel at once.
 */
public class GroveI2CHub8 implements I2CDevice {

    private final static int TCA9548_ADDRESS = 0x70;

    /**
     * Bitmask values for the eight downstream channels. The constants
     * are named with a leading underscore because Java identifiers
     * cannot start with a digit.
     */
    public enum TCAChannel {
        /** Downstream channel 0 (bit {@code 0x01}). */ _0(0x1),
        /** Downstream channel 1 (bit {@code 0x02}). */ _1(0x2),
        /** Downstream channel 2 (bit {@code 0x04}). */ _2(0x4),
        /** Downstream channel 3 (bit {@code 0x08}). */ _3(0x8),
        /** Downstream channel 4 (bit {@code 0x10}). */ _4(0x10),
        /** Downstream channel 5 (bit {@code 0x20}). */ _5(0x20),
        /** Downstream channel 6 (bit {@code 0x40}). */ _6(0x40),
        /** Downstream channel 7 (bit {@code 0x80}). */ _7(0x80);

        private final int value;

        TCAChannel(int value) { this.value = value; }

        /** @return the bitmask for this channel */
        public int getValue() { return value; }
    }

    private final I2C hub;

    private int channels; // uint16_t

    /**
     * Opens the I²C handle and starts with every downstream channel
     * closed.
     *
     * @param pi4j Pi4J context
     * @throws IOException if the chip cannot be opened
     */
    public GroveI2CHub8(Context pi4j) throws IOException {
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                               .id("Grovepi-plus" + TCA9548_ADDRESS)
                               .name("My I2C Bus " + TCA9548_ADDRESS)
                               .bus(GrovePi4J.I2C_BUS)
                               .device(TCA9548_ADDRESS)
                               .build();

        this.hub = pi4j.create(config);

        this.init();
    }

    private void init() { closeAll(); }

    /** Closes every downstream channel — no device sees the upstream bus. */
    public void closeAll() {
        this.channels = 0x00;
        hub.write(channels);
    }

    /**
     * Opens every downstream channel — every device sees the upstream
     * bus. Useful for broadcasts; not safe when downstream devices share
     * an address.
     */
    public void openAll() {
        this.channels = 0xFF;
        hub.write(channels);
    }

    /**
     * Toggles the supplied channel via XOR. Note that this only
     * <em>closes</em> the channel if it was previously open; calling it
     * twice for the same channel re-opens it.
     *
     * @param channel the channel to toggle
     */
    public void closeChannel(TCAChannel channel) {
        this.channels ^= channel.getValue();
        hub.write(channels);
    }

    /**
     * Opens the supplied channel without affecting the others.
     *
     * @param channel the channel to open
     */
    public void openChannel(TCAChannel channel) {
        this.channels |= channel.getValue();
        hub.write(channels);
    }
}
