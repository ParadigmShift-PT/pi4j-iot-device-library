package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import java.util.concurrent.locks.LockSupport;

public class GroveUltrasonicRanger extends DigitalInputDevice {

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

    public long measureInCentimeters() {
        return measureInCentimeters(1000000L); // microseconds
    }

    public long measureInCentimeters(long timeout) {
        long range = duration(timeout) / 29 / 2;
        return range;
    }

    public long measureInMillimeters() {
        return measureInMillimeters(1000000L);
    }

    public long measureInMillimeters(long timeout) {
        long range = duration(timeout) * 5 / 29;
        return range;
    }

    public long measureInInches() { return measureInInches(1000000L); }

    public long measureInInches(long timeout) {
        long range = duration(timeout) / 74 / 2;
        return range;
    }
}
