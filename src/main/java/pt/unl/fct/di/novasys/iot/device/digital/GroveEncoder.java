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

public class GroveEncoder extends DigitalInputDevice {
    public static enum Rotation {
        NONE(0),
        CLOCKWISE(1),
        COUNTER_CLOCKWISE(2);

        public final int value;

        Rotation(int value) { this.value = value; }

        public int getValue() { return this.value; }

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

    public Rotation getRotation() {
        return Rotation.fromValue(rotation.getAndSet(Rotation.NONE.getValue()));
    }
}
