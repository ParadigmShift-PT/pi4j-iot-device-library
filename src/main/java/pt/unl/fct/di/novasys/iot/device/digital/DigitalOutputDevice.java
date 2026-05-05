package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.Digital;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;

import io.helins.linux.gpio.*;
import pt.unl.fct.di.novasys.iot.device.DigitalDevice;

/**
 * Generic wrapper for any Grove peripheral connected to a digital output
 * pin (LED, vibration motor, relay, …). Provides {@link #setHigh()} /
 * {@link #setLow()} / {@link #setState(DigitalState)} convenience methods
 * and optional {@link GpioBuffer} / {@link GpioHandle} fields for callers
 * that batch writes through libgpiod.
 */
public class DigitalOutputDevice extends DigitalDevice {
    private GpioBuffer buffer;
    private GpioHandle handle;

    /**
     * Full constructor.
     *
     * @param pi4j       Pi4J context
     * @param clkCfg     clock output configuration, or {@code null}
     * @param dataInCfg  optional input configuration for bidirectional pins,
     *                   or {@code null}
     * @param dataOutCfg data output configuration (required)
     * @param ID         caller-assigned device identifier
     */
    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                               DigitalInputConfig dataInCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        super(pi4j, clkCfg, dataInCfg, null, dataOutCfg, dataOutCfg.name(), ID);
    }

    /**
     * Constructs a clock-driven output device.
     *
     * @param pi4j       Pi4J context
     * @param clkCfg     clock output configuration
     * @param dataOutCfg data output configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, clkCfg, null, dataOutCfg, ID);
    }

    /**
     * Constructs a bidirectional output device that can flip its line into
     * input mode (see {@link DigitalDevice#setDataPinInput()}).
     *
     * @param pi4j       Pi4J context
     * @param dataInCfg  data input configuration
     * @param dataOutCfg data output configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalOutputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, null, dataInCfg, dataOutCfg, ID);
    }

    /**
     * Constructs a single-pin output device.
     *
     * @param pi4j       Pi4J context
     * @param dataOutCfg data output configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig dataOutCfg,
                               int ID) {
        this(pi4j, null, null, dataOutCfg, ID);
    }

    /**
     * Convenience constructor that builds the {@link DigitalOutputConfig}
     * for a single-pin device from a name and BCM pin number.
     *
     * @param pi4j Pi4J context
     * @param name human-readable name (becomes the Pi4J handle id)
     * @param line BCM pin number
     * @param ID   caller-assigned device identifier
     */
    public DigitalOutputDevice(Context pi4j, String name, int line, int ID) {
        this(pi4j,
             DigitalOutput.newConfigBuilder(pi4j)
                 .id(name)
                 .name(name + " — " + ID)
                 .address(line)
                 .build(),
             ID);
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    /** Drives the output pin to {@link DigitalState#HIGH}. */
    public void setHigh() {
        dataOut.state(DigitalState.HIGH);
    }

    /** Drives the output pin to {@link DigitalState#LOW}. */
    public void setLow() {
        dataOut.state(DigitalState.LOW);
    }

    /**
     * Drives the output pin high or low.
     *
     * @param state {@code true} for {@link DigitalState#HIGH},
     *              {@code false} for {@link DigitalState#LOW}
     */
    public void setState(boolean state) {
        dataOut.state(state ? DigitalState.HIGH : DigitalState.LOW);
    }

    /**
     * Drives the output pin to an explicit {@link DigitalState}.
     *
     * @param state the desired state
     */
    public void setState(DigitalState state) {
        dataOut.state(state);
    }

    /** @return the libgpiod buffer attached to this device, or {@code null} if none */
    public GpioBuffer getBuffer() { return this.buffer; }

    /**
     * Attaches a libgpiod buffer used for batched writes.
     *
     * @param buffer the buffer to attach
     */
    public void setBuffer(GpioBuffer buffer) { this.buffer = buffer; }

    /** @return the libgpiod handle attached to this device, or {@code null} if none */
    public GpioHandle getHandle() { return this.handle; }

    /**
     * Attaches a libgpiod handle used for batched writes.
     *
     * @param handle the handle to attach
     */
    public void setHandle(GpioHandle handle) { this.handle = handle; }
}
