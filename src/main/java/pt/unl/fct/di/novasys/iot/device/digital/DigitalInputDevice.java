package pt.unl.fct.di.novasys.iot.device.digital;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import io.helins.linux.gpio.*;
import java.io.IOException;
import pt.unl.fct.di.novasys.iot.device.DigitalDevice;

public class DigitalInputDevice extends DigitalDevice {
    private GpioEventHandle eventHandle;

    public DigitalInputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                              DigitalInputConfig dataInCfg,
                              DigitalInputConfig dataIn2Cfg,
                              DigitalOutputConfig dataOutCfg, int ID) {
        super(pi4j, clkCfg, dataInCfg, dataIn2Cfg, dataOutCfg, dataInCfg.name(), ID);
    }

    public DigitalInputDevice(Context pi4j, DigitalOutputConfig clkCfg,
                              DigitalInputConfig dataInCfg, int ID) {
        this(pi4j, clkCfg, dataInCfg, null, null, ID);
    }

    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              DigitalInputConfig dataIn2Cfg, int ID) {
        this(pi4j, null, dataInCfg, dataIn2Cfg, null, ID);
    }

    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              DigitalOutputConfig dataOutCfg, int ID) {
        this(pi4j, null, dataInCfg, null, dataOutCfg, ID);
    }

    public DigitalInputDevice(Context pi4j, DigitalInputConfig dataInCfg,
                              int ID) {
        this(pi4j, null, dataInCfg, null, null, ID);
    }

    public DigitalInputDevice(Context pi4j, String name, int line, int ID) {
        this(pi4j,
             DigitalInput.newConfigBuilder(pi4j)
                 .id(name)
                 .name(name + " — " + ID)
                 .address(line)
                 .build(),
             ID);
    }

    public GpioEventHandle requestEventHandle(GpioDevice device,
                                              GpioEdgeDetection edgeDetection)
        throws IOException {
        this.eventHandle =
            device.requestEvent(new GpioEventRequest(this.line, edgeDetection));

        return this.eventHandle;
    }

    public void addHandle(GpioEventWatcher watcher) throws Exception {
        if (this.eventHandle == null) {
            throw new Exception("Device handle hasn't been initialized yet.");
        }

        watcher.addHandle(this.eventHandle, this.ID);
    }

    public void removeHandle(GpioEventWatcher watcher) throws Exception {
        if (this.eventHandle == null) {
            throw new Exception("Device handle hasn't been initialized yet.");
        }

        watcher.removeHandle(this.eventHandle);
    }

    public boolean isOn() { return dataIn.isOn(); }

    public boolean isOff() { return dataIn.isOff(); }

    public boolean isHigh() { return dataIn.isHigh(); }

    public boolean isLow() { return dataIn.isLow(); }

    public DigitalState getState() { return dataIn.state(); }

    public GpioEventHandle getEventHandle() { return eventHandle; }

    public void setEventHandle(GpioEventHandle handle) {
        this.eventHandle = handle;
    }
}
