package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import io.helins.linux.gpio.*;
import java.io.IOException;
import pt.unl.fct.di.novasys.iot.device.DigitalDevice;

/**
 * Generic wrapper for any Grove peripheral connected to a digital input
 * pin (button, tilt switch, PIR motion sensor, flame sensor, line finder,
 * touch sensor, …). Reads pin state and, optionally, integrates with a
 * libgpiod {@link GpioEventWatcher} so the caller can receive interrupt-
 * driven edge events instead of polling.
 *
 * <p>For Grove devices that only expose a single data line, use the
 * {@link #DigitalInputDevice(Context, String, int, int)} convenience
 * constructor.
 */
public class DigitalInputDevice extends DigitalDevice {
    private GpioEventHandle eventHandle;

    /**
     * Full constructor exposing every pin configuration field of
     * {@link DigitalDevice}.
     *
     * @param pi4j       Pi4J context
     * @param clkCfg     clock output configuration, or {@code null}
     * @param dataInCfg  primary data input configuration (required)
     * @param dataIn2Cfg secondary data input configuration, or {@code null}
     * @param dataOutCfg data output configuration, or {@code null}
     * @param ID         caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                              DigitalInputConfig dataInCfg,
                              DigitalInputConfig dataIn2Cfg,
                              DigitalOutputConfig dataOutCfg, int ID) {
        super(pi4j, clkCfg, dataInCfg, dataIn2Cfg, dataOutCfg, dataInCfg.name(), ID);
    }

    /**
     * Constructs a clock-driven input device with a single data line.
     *
     * @param pi4j       Pi4J context
     * @param clkCfg     clock output configuration
     * @param dataInCfg  data input configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                              DigitalInputConfig dataInCfg, int ID) {
        this(pi4j, clkCfg, dataInCfg, null, null, ID);
    }

    /**
     * Constructs an input device with two data lines (e.g., a quadrature
     * encoder).
     *
     * @param pi4j       Pi4J context
     * @param dataInCfg  primary data input configuration
     * @param dataIn2Cfg secondary data input configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              DigitalInputConfig dataIn2Cfg, int ID) {
        this(pi4j, null, dataInCfg, dataIn2Cfg, null, ID);
    }

    /**
     * Constructs a bidirectional device that can flip its data line
     * between input and output modes (see
     * {@link DigitalDevice#setDataPinInput()} /
     * {@link DigitalDevice#setDataPinOutput()}).
     *
     * @param pi4j       Pi4J context
     * @param dataInCfg  data input configuration
     * @param dataOutCfg data output configuration
     * @param ID         caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, null, dataInCfg, null, dataOutCfg, ID);
    }

    /**
     * Constructs a single-pin input device.
     *
     * @param pi4j      Pi4J context
     * @param dataInCfg data input configuration
     * @param ID        caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              int ID) {
        this(pi4j, null, dataInCfg, null, null, ID);
    }

    /**
     * Convenience constructor that builds the {@link DigitalInputConfig}
     * for a single-pin device from a name and BCM pin number.
     *
     * @param pi4j Pi4J context
     * @param name human-readable name (becomes the Pi4J handle id)
     * @param line BCM pin number
     * @param ID   caller-assigned device identifier
     */
    public DigitalInputDevice(Context pi4j, String name, int line, int ID) {
        this(pi4j,
             DigitalInput.newConfigBuilder(pi4j)
                 .id(name)
                 .name(name + " — " + ID)
                 .address(line)
                 .build(),
             ID);
    }

    /**
     * Requests an event handle on the underlying libgpiod device so this
     * input can deliver edge-triggered notifications. Must be called
     * before {@link #addHandle(GpioEventWatcher)}.
     *
     * @param device        the libgpiod chip handle
     * @param edgeDetection which edge(s) (rising / falling / both) to watch
     * @return the newly created event handle
     * @throws IOException if the underlying libgpiod request fails
     */
    public GpioEventHandle requestEventHandle(GpioDevice device,
                                              GpioEdgeDetection edgeDetection)
        throws IOException {
        this.eventHandle =
            device.requestEvent(new GpioEventRequest(this.line, edgeDetection));

        return this.eventHandle;
    }

    /**
     * Registers this device's event handle with a watcher so the watcher
     * will dispatch edge events tagged with this device's ID.
     *
     * @param watcher the event watcher
     * @throws Exception if {@link #requestEventHandle(GpioDevice, GpioEdgeDetection)}
     *                   has not yet been called
     */
    public void addHandle(GpioEventWatcher watcher) throws Exception {
        if (this.eventHandle == null) {
            throw new Exception("Device handle hasn't been initialized yet.");
        }

        watcher.addHandle(this.eventHandle, this.ID);
    }

    /**
     * Removes this device's event handle from a watcher.
     *
     * @param watcher the event watcher
     * @throws Exception if {@link #requestEventHandle(GpioDevice, GpioEdgeDetection)}
     *                   has not yet been called
     */
    public void removeHandle(GpioEventWatcher watcher) throws Exception {
        if (this.eventHandle == null) {
            throw new Exception("Device handle hasn't been initialized yet.");
        }

        watcher.removeHandle(this.eventHandle);
    }

    /** @return {@code true} if the pin is reading {@link DigitalState#HIGH} */
    public boolean isOn() { return dataIn.isOn(); }

    /** @return {@code true} if the pin is reading {@link DigitalState#LOW} */
    public boolean isOff() { return dataIn.isOff(); }

    /** @return {@code true} if the pin is reading {@link DigitalState#HIGH} */
    public boolean isHigh() { return dataIn.isHigh(); }

    /** @return {@code true} if the pin is reading {@link DigitalState#LOW} */
    public boolean isLow() { return dataIn.isLow(); }

    /** @return the current pin state */
    public DigitalState getState() { return dataIn.state(); }

    /** @return the current libgpiod event handle, or {@code null} if none has been requested */
    public GpioEventHandle getEventHandle() { return eventHandle; }

    /**
     * Replaces the current event handle. Mostly useful for tests and when
     * adopting an externally managed handle.
     *
     * @param handle the handle to install
     */
    public void setEventHandle(GpioEventHandle handle) {
        this.eventHandle = handle;
    }
}
