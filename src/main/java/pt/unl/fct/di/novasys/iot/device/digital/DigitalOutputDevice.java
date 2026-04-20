package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.Digital;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;

import io.helins.linux.gpio.*;
import pt.unl.fct.di.novasys.iot.device.DigitalDevice;

public class DigitalOutputDevice extends DigitalDevice {
    private GpioBuffer buffer;
    private GpioHandle handle;

    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                               DigitalInputConfig dataInCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        super(pi4j, clkCfg, dataInCfg, null, dataOutCfg, dataOutCfg.name(), ID);
    }

    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, clkCfg, null, dataOutCfg, ID);
    }

    public DigitalOutputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                               DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, null, dataInCfg, dataOutCfg, ID);
    }

    public DigitalOutputDevice(Context pi4j, DigitalOutputConfig dataOutCfg,
                               int ID) {
        this(pi4j, null, null, dataOutCfg, ID);
    }

    public DigitalOutputDevice(Context pi4j, String name, int line, int ID) {
        this(pi4j,
             DigitalOutput.newConfigBuilder(pi4j)
                 .id(name)
                 .name(name + " — " + ID)
                 .address(line)
                 .build(),
             ID);
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    public void setHigh() {
        dataOut.state(DigitalState.HIGH);
    }

    public void setLow() {
        dataOut.state(DigitalState.LOW);
    }

    public void setState(boolean state) {
        dataOut.state(state ? DigitalState.HIGH : DigitalState.LOW);
    }

    public void setState(DigitalState state) {
        dataOut.state(state);
    }

    public GpioBuffer getBuffer() { return this.buffer; }

    public void setBuffer(GpioBuffer buffer) { this.buffer = buffer; }

    public GpioHandle getHandle() { return this.handle; }

    public void setHandle(GpioHandle handle) { this.handle = handle; }
}
