package pt.unl.fct.di.novasys.iot.device;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;

/**
 * Abstract base for every device that talks over GPIO digital pins. Holds
 * the pin configuration and the live Pi4J handles, and provides shared
 * primitives that subclasses use to drive bit-banged Grove protocols
 * ({@link #delayMicroseconds(int)}, {@link #setDataPinInput()},
 * {@link #setDataPinOutput()}).
 *
 * <p>A concrete digital device may use up to four pins:
 * <ul>
 *   <li>a clock output ({@code clk}) for synchronous protocols;</li>
 *   <li>a primary data input ({@code dataIn});</li>
 *   <li>a secondary data input ({@code dataIn2}) for two-line sensors
 *       (e.g., a quadrature encoder);</li>
 *   <li>a data output ({@code dataOut}) for output-only or bidirectional
 *       lines.</li>
 * </ul>
 *
 * <p>Configurations may be passed as {@code null} for unused pins. The
 * device's <em>line number</em> is taken from the first non-{@code null}
 * configuration in the order: clock → data-in → data-out.
 */
public abstract class DigitalDevice implements Device {

    /** Human-readable name of this device. */
    protected final String name;
    /** Caller-assigned device identifier (typically unique within an
     *  application's device registry). */
    protected final int ID;
    /** GPIO line / address used as the device's primary pin. */
    protected int line;
    /** The owning Pi4J context — used to create and shut down handles. */
    protected final Context pi4j;

    /** Live data-input handle, or {@code null} when the pin is in output mode. */
    protected DigitalInput dataIn;
    /** Optional second data-input handle for two-line sensors. */
    protected DigitalInput dataIn2;
    /** Live data-output handle, or {@code null} when the pin is in input mode. */
    protected DigitalOutput dataOut;
    /** Live clock-output handle, or {@code null} for non-synchronous devices. */
    protected DigitalOutput clk;

    /** Pi4J configuration kept around so the data pin can be reopened in input mode. */
    protected DigitalInputConfig dataInCfg;
    /** Pi4J configuration kept around so the data pin can be reopened in output mode. */
    protected DigitalOutputConfig dataOutCfg;
    /** Pi4J configuration for the clock pin. */
    protected DigitalOutputConfig clkCfg;

    /**
     * Constructs a digital device, opening the handles for whichever pin
     * configurations are non-{@code null}. Pass {@code null} for unused
     * pins.
     *
     * @param pi4j       Pi4J context
     * @param clkCfg     clock output configuration, or {@code null}
     * @param dataInCfg  primary data input configuration, or {@code null}
     * @param dataIn2Cfg secondary data input configuration, or {@code null}
     * @param dataOutCfg data output configuration, or {@code null}
     * @param name       human-readable name
     * @param ID         caller-assigned device identifier
     */
    public DigitalDevice(Context pi4j, DigitalOutputConfig clkCfg,
                         DigitalInputConfig dataInCfg,
                         DigitalInputConfig dataIn2Cfg,
                         DigitalOutputConfig dataOutCfg, String name, int ID) {
        this.pi4j = pi4j;
        this.name = name;
        this.ID = ID;

        // clk sets the line number because it comes first in the Grove
        // interface
        if (clkCfg != null) {
            this.clkCfg = clkCfg;
            this.clk = pi4j.create(clkCfg);
            this.line = clkCfg.address();
        }

        if (dataInCfg != null) {
            this.dataIn = pi4j.create(dataInCfg);

            if (dataIn2Cfg != null)
                this.dataIn2 = pi4j.create(dataIn2Cfg);

            if (clkCfg == null)
                this.line = dataInCfg.address();

        } else if (dataOutCfg != null) {
            this.dataOut = pi4j.create(dataOutCfg);

            if (clkCfg == null)
                this.line = dataOutCfg.address();

        } else {
            // this should never happen, throw exception?
        }
    }

    /** @return the device's human-readable name */
    public String getName() { return this.name; }

    /** @return the caller-assigned device identifier */
    public int getID() { return this.ID; }

    /**
     * @return the device's primary GPIO line number — the clock line when a
     *         clock pin is configured, otherwise the data-in or data-out line
     */
    public int getLineNumber() { return this.line; };

    /**
     * Busy-waits for the given number of microseconds. Used by Grove device
     * protocols (e.g., DHT11/22) that require sub-millisecond timing
     * resolution that {@link Thread#sleep(long)} cannot provide.
     *
     * <p>This method spins on {@link System#nanoTime()} and does not yield
     * the CPU; keep delays short.
     *
     * @param us microseconds to wait
     */
    protected void delayMicroseconds(int us) {
        long waitUntil = System.nanoTime() + (us * 1_000);
        while (waitUntil > System.nanoTime())
            ;
    }

    /**
     * Switches the data pin from input mode to output mode. Closes the
     * input handle, waits 50 µs for the line to settle, then opens an
     * output handle using {@link #dataOutCfg}.
     */
    protected void setDataPinOutput() {
        if (dataIn != null) {
            pi4j.shutdown(dataIn.getId());
            dataIn = null;
        }

        delayMicroseconds(50);

        if (dataOut == null) {
            dataOut = pi4j.create(dataOutCfg);
        }
    }

    /**
     * Switches the data pin from output mode to input mode. Closes the
     * output handle, waits 50 µs for the line to settle, then opens an
     * input handle using {@link #dataInCfg}.
     */
    protected void setDataPinInput() {
        if (dataOut != null) {
            pi4j.shutdown(dataOut.getId());
            dataOut = null;
        }

        delayMicroseconds(50);

        if (dataIn == null) {
            dataIn = pi4j.create(dataInCfg);
        }
    }

    /** @return the live data-output handle, or {@code null} if the pin is in input mode */
    public DigitalOutput getDataOutputPin() { return this.dataOut; }

    /** @return the live data-input handle, or {@code null} if the pin is in output mode */
    public DigitalInput getDataInputPin() { return this.dataIn; }

    /** @return the clock-output handle, or {@code null} for non-synchronous devices */
    public DigitalOutput getClockOutputPin() { return this.clk; }
}
