package pt.unl.fct.di.novasys.iot.device.analog;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;

/**
 * Singleton wrapper for the Grove Base Hat's onboard ADC. The Grove Base
 * Hat exposes eight analogue input channels through a single I²C device
 * (address {@code 0x04} for the STM32 variant, {@code 0x08} for the MM32),
 * so all {@link AnalogInputDevice} instances share one underlying handle.
 *
 * <p>Each channel can be read in three forms (selected by the register
 * offset on the I²C device):
 * <ul>
 *   <li>{@link #readRaw(int) raw} — the 12-bit ADC value (0 – 4095);</li>
 *   <li>{@link #readVoltage(int) voltage} — millivolts;</li>
 *   <li>{@link #readRatio(int) ratio} — input vs. power supply voltage,
 *       in 0.1% units (0 – 1000).</li>
 * </ul>
 *
 * <p>This class is a singleton because the underlying Pi4J I²C handle
 * cannot be opened twice on the same bus / address. Use
 * {@link #initialize(Context, int)} once at start-up, or call
 * {@link #getInstance(Context, int)} which initialises lazily on first
 * use. {@link #getInstance()} throws if neither has been called.
 */
public class GroveBaseHatADC {
    private final I2C i2c;
    private static GroveBaseHatADC instance;

    private GroveBaseHatADC(Context pi4j, int address) {
        this.i2c =
            pi4j.create(I2C.newConfigBuilder(pi4j)
                            .id("grove-hat-adc")
                            .name("Grove Base Hat ADC")
                            .bus(1)
                            .device(address) // 0x04 for STM32, 0x08 for MM32
                            .build());
    }

    /**
     * Initialises the singleton. Must be called at most once per process.
     *
     * @param pi4j    Pi4J context
     * @param address I²C address — {@code 0x04} for STM32-based hats,
     *                {@code 0x08} for MM32-based hats
     * @throws IllegalStateException if already initialised
     */
    public static void initialize(Context pi4j, int address) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new GroveBaseHatADC(pi4j, address);
    }

    /**
     * Returns the singleton, initialising it on first call.
     *
     * @param pi4j    Pi4J context
     * @param address I²C address ({@code 0x04} or {@code 0x08})
     * @return the singleton instance
     */
    public static GroveBaseHatADC getInstance(Context pi4j, int address) {
        if (instance == null) {
            initialize(pi4j, address);
        }
        return instance;
    }

    /**
     * Returns the singleton, assuming it has already been initialised.
     *
     * @return the singleton instance
     * @throws IllegalStateException if never initialised
     */
    public static GroveBaseHatADC getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "Must call initialize(Context pi4j, int address) first or "
                + "use getInstance(Context pi4j, int address)");
        }
        return instance;
    }

    /**
     * Reads the raw 12-bit ADC value of an analogue channel.
     *
     * @param channel channel number (0 – 7)
     * @return the raw value (0 – 4095)
     */
    public int readRaw(int channel) {
        int addr = 0x10 + channel;
        return readRegister(addr);
    }

    /**
     * Reads the channel voltage in millivolts.
     *
     * @param channel channel number (0 – 7)
     * @return the voltage in mV
     */
    public int readVoltage(int channel) {
        int addr = 0x20 + channel;
        return readRegister(addr);
    }

    /**
     * Reads the ratio of the channel's input voltage to the supply
     * voltage, expressed in 0.1% units (so the full scale is 1000).
     *
     * @param channel channel number (0 – 7)
     * @return the ratio in 0.1% units (0 – 1000)
     */
    public int readRatio(int channel) {
        int addr = 0x30 + channel;
        return readRegister(addr);
    }

    private int readRegister(int register) {
        int word = i2c.readRegisterWord(register);

        // swap bytes to little-endian
        return ((word & 0xFF) << 8) | ((word >> 8) & 0xFF);
    }
}
