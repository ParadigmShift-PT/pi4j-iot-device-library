package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import java.util.concurrent.locks.LockSupport;

/**
 * Driver for the Grove Ultrasonic Ranger (HC-SR04 family). Uses a single
 * GPIO pin that flips between output and input mode: a 5 µs pulse on the
 * pin triggers the burst, then the pin is reopened as an input to time
 * the echo. Distance is computed from the speed of sound (~340 m/s).
 *
 * <p>Each measurement takes up to {@code timeout} microseconds and busy-
 * waits on the calling thread. Returning {@code 0} means the echo never
 * arrived — typical for objects beyond ~4 m or with poor acoustic
 * reflection.
 */
public class GroveUltrasonicRanger extends DigitalInputDevice {

    /**
     * Constructs an ultrasonic ranger driven by a single bidirectional pin.
     *
     * @param pi4j Pi4J context
     * @param name human-readable name
     * @param line BCM pin number (used as both trigger output and echo input)
     * @param ID   caller-assigned device identifier
     */
    public GroveUltrasonicRanger(Context pi4j, String name, int line, int ID) {
        super(pi4j,
              DigitalInput.newConfigBuilder(pi4j)
                  .id(name + "_in")
                  .name(name + " — " + ID)
                  .address(line)
                  .build(),
              DigitalOutput.newConfigBuilder(pi4j)
                  .id(name + "_out")
                  .name(name + " — " + ID)
                  .address(line)
                  .initial(DigitalState.LOW)
                  .build(),
              ID);
    }

    private long pulseIn(long timeout) {
        long start = System.nanoTime();

        while (dataIn.isHigh()) {
            if ((System.nanoTime() - start) / 1000 >= timeout) {
                return 0;
            }
        }

        while (dataIn.isLow()) {
            if ((System.nanoTime() - start) / 1000 >= timeout) {
                return 0;
            }
        }

        long pulseStart = System.nanoTime();

        while (dataIn.isHigh()) {
            if ((System.nanoTime() - start) / 1000 >= timeout) {
                return 0;
            }
        }

        long pulseEnd = System.nanoTime();
        return (pulseEnd - pulseStart) / 1000;
    }

    private long duration(long timeout) {
        // shutdown removes the pin address from the pi4j context, making it
        // available to register again as a different type
        setDataPinOutput();

        dataOut.state(DigitalState.LOW);
        delayMicroseconds(2);
        dataOut.state(DigitalState.HIGH);
        delayMicroseconds(5);
        dataOut.state(DigitalState.LOW);

        setDataPinInput();

        return pulseIn(timeout);
    }

    /**
     * Measures distance in centimetres with a 1 s timeout.
     * @return distance in cm, or 0 on timeout
     */
    public long measureInCentimeters() {
        return measureInCentimeters(1000000L); // microseconds
    }

    /**
     * Measures distance in centimetres.
     *
     * @param timeout maximum wait for the echo, in microseconds
     * @return distance in cm, or 0 on timeout
     */
    public long measureInCentimeters(long timeout) {
        long range = duration(timeout) / 29 / 2;
        return range;
    }

    /**
     * Measures distance in millimetres with a 1 s timeout.
     * @return distance in mm, or 0 on timeout
     */
    public long measureInMillimeters() {
        return measureInMillimeters(1000000L);
    }

    /**
     * Measures distance in millimetres.
     *
     * @param timeout maximum wait for the echo, in microseconds
     * @return distance in mm, or 0 on timeout
     */
    public long measureInMillimeters(long timeout) {
        long range = duration(timeout) * 5 / 29;
        return range;
    }

    /**
     * Measures distance in inches with a 1 s timeout.
     * @return distance in inches, or 0 on timeout
     */
    public long measureInInches() { return measureInInches(1000000L); }

    /**
     * Measures distance in inches.
     *
     * @param timeout maximum wait for the echo, in microseconds
     * @return distance in inches, or 0 on timeout
     */
    public long measureInInches(long timeout) {
        long range = duration(timeout) / 74 / 2;
        return range;
    }
}
