package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.PullResistance;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Driver for the Grove rotary encoder. Decodes the two-line quadrature
 * signal in the background and exposes the latest direction via
 * {@link #getRotation()}, which clears the pending event on read so each
 * rotation is reported once.
 *
 * <p>Two modes are available:
 * <ul>
 *   <li><b>threshold = 0</b>: every detent is reported as a rotation;</li>
 *   <li><b>threshold &gt; 0</b>: only every {@code N}-th detent in the
 *       same direction is reported, smoothing out noisy mechanical
 *       encoders. Direction changes reset the count.</li>
 * </ul>
 *
 * <p>A 1 ms-period sampler arms the listener whenever both lines are
 * high (the encoder's resting state), so rotation detection is edge-aware
 * without missing transitions on bouncy contacts.
 */
public class GroveEncoder extends DigitalInputDevice {
    /** Direction of the most recent rotation. */
    public static enum Rotation {
        /** No rotation observed (or already consumed by {@link #getRotation()}). */
        NONE(0),
        /** Clockwise (right) rotation. */
        CLOCKWISE(1),
        /** Counter-clockwise (left) rotation. */
        COUNTER_CLOCKWISE(2);

        /** Wire-format integer value for this rotation. */
        public final int value;

        Rotation(int value) { this.value = value; }

        /** @return the wire-format integer for this rotation */
        public int getValue() { return this.value; }

        /**
         * Looks up a rotation by its integer value. Unknown values return
         * {@link #NONE}.
         *
         * @param value the integer value
         * @return the matching rotation, or {@link #NONE}
         */
        public static Rotation fromValue(int value) {
            for (Rotation r : values()) {
                if (r.value == value) {
                    return r;
                }
            }
            return NONE;
        }
    }

    // only count a rotation after this many ticks
    private final int threshold;

    private final AtomicInteger rotation;
    private final AtomicBoolean readyMsg;
    private final ScheduledExecutorService scheduler;

    private int ticks;
    private Rotation lastRotation;

    /**
     * Constructs an encoder with explicit detent thresholding.
     *
     * @param pi4j      Pi4J context
     * @param name      human-readable name
     * @param line      first encoder pin (the second pin is {@code line + 1})
     * @param ID        caller-assigned device identifier
     * @param threshold number of consecutive same-direction detents before a
     *                  rotation is reported; pass {@code 0} to report every detent
     */
    public GroveEncoder(Context pi4j, String name, int line, int ID,
                        int threshold) {
        super(pi4j,
              DigitalInput.newConfigBuilder(pi4j)
                  .id(name + "_A")
                  .name(name + " _A_ " + ID)
                  .address(line)
                  .pull(PullResistance.PULL_UP)
                  .build(),
              DigitalInput.newConfigBuilder(pi4j)
                  .id(name + "_B")
                  .name(name + " _B_ " + ID + 1)
                  .address(line + 1)
                  .pull(PullResistance.PULL_UP)
                  .build(),
              ID);
        this.threshold = threshold;

        this.rotation = new AtomicInteger(Rotation.NONE.getValue());
        this.readyMsg = new AtomicBoolean(false);

        this.ticks = 0;
        this.lastRotation = Rotation.NONE;

        // not great maybe but alas
        this.scheduler = Executors.newScheduledThreadPool(1);

        if (threshold > 0) {
            this.dataIn.addListener(this::onChangeThreshold);
            this.dataIn2.addListener(this::onChangeThreshold);
        } else {
            this.dataIn.addListener(this::onChange);
            this.dataIn2.addListener(this::onChange);
        }

        scheduler.scheduleAtFixedRate(() -> {
            boolean a = this.dataIn.state() == DigitalState.HIGH;
            boolean b = this.dataIn2.state() == DigitalState.HIGH;

            if (a && b) {
                readyMsg.set(true);
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    /**
     * Constructs an encoder that reports every detent (no thresholding).
     *
     * @param pi4j Pi4J context
     * @param name human-readable name
     * @param line first encoder pin (the second pin is {@code line + 1})
     * @param ID   caller-assigned device identifier
     */
    public GroveEncoder(Context pi4j, String name, int line, int ID) {
        this(pi4j, name, line, ID, 0);
    }

    private void onChange(DigitalStateChangeEvent<DigitalInput> event) {
        if (readyMsg.get()) {
            boolean a = this.dataIn.state() == DigitalState.HIGH;
            boolean b = this.dataIn2.state() == DigitalState.HIGH;

            if (a && !b) {
                rotation.set(Rotation.CLOCKWISE.getValue()); // right
                readyMsg.set(false);
            } else if (!a && b) {
                rotation.set(Rotation.COUNTER_CLOCKWISE.getValue()); // left
                readyMsg.set(false);
            }
        }
    }

    private void
    onChangeThreshold(DigitalStateChangeEvent<DigitalInput> event) {
        if (readyMsg.get()) {
            boolean a = this.dataIn.state() == DigitalState.HIGH;
            boolean b = this.dataIn2.state() == DigitalState.HIGH;

            Rotation currentRotation = Rotation.NONE;
            if (a && !b)
                currentRotation = Rotation.CLOCKWISE;
            else if (!a && b)
                currentRotation = Rotation.COUNTER_CLOCKWISE;

            if (currentRotation != Rotation.NONE) {
                if (currentRotation != lastRotation) {
                    ticks = 1; // reset on direction change but count the tick
                    lastRotation = currentRotation;
                } else if (++ticks >= threshold) {
                    rotation.set(currentRotation.getValue());
                    ticks = 0;
                }

                readyMsg.set(false);
            }
        }
    }

    /**
     * Returns the most recently observed rotation and atomically clears
     * the pending event, so each rotation is reported once.
     *
     * @return the latest rotation, or {@link Rotation#NONE} if nothing
     *         has happened since the last call
     */
    public Rotation getRotation() {
        return Rotation.fromValue(rotation.getAndSet(Rotation.NONE.getValue()));
    }
}
