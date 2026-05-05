package pt.unl.fct.di.novasys.iot.device.analog;

import com.pi4j.context.Context;

/**
 * Generic wrapper for any Grove peripheral connected to an analogue input
 * channel of the Grove Base Hat (light sensor, gas sensor, loudness,
 * GSR, …). The underlying ADC is shared via the {@link GroveBaseHatADC}
 * singleton; this class just holds the channel number plus a name / ID
 * for identification.
 *
 * <p>The first {@code AnalogInputDevice} constructed in a process
 * lazily initialises {@link GroveBaseHatADC} at I²C address {@code 0x04}
 * (the STM32 variant). To use the MM32 variant ({@code 0x08}), call
 * {@link GroveBaseHatADC#initialize(Context, int)} explicitly before
 * constructing any analogue devices.
 */
public class AnalogInputDevice {

    private String name;
    private int ID;
    private int line;

    private GroveBaseHatADC hat;

    /**
     * Constructs an analogue input device.
     *
     * @param pi4j Pi4J context
     * @param name human-readable name
     * @param line analogue channel number (0 – 7) on the Grove Base Hat
     * @param ID   caller-assigned device identifier
     */
    public AnalogInputDevice(Context pi4j, String name, int line, int ID) {
        this.name = name;
        this.ID = ID;
        this.line = line;

        try {
            this.hat = GroveBaseHatADC.getInstance(pi4j, 0x04);
        } catch (IllegalStateException e) {
            // already initialized
        }
    }

    /** @return the raw 12-bit ADC value (0 – 4095) */
    public int readRaw() { return hat.readRaw(this.line); }

    /** @return the channel voltage in millivolts */
    public int readVoltage() { return hat.readVoltage(this.line); }

    /** @return the input/supply voltage ratio in 0.1% units (0 – 1000) */
    public int read() { return hat.readRatio(this.line); }

    /** @return the device's human-readable name */
    public String getName() { return name; }

    /** @return the caller-assigned device identifier */
    public int getID() { return ID; }

    /** @return the analogue channel number on the Grove Base Hat */
    public int getLine() { return line; }

    /** @return the underlying shared ADC singleton */
    public GroveBaseHatADC getHat() { return hat; }
}
