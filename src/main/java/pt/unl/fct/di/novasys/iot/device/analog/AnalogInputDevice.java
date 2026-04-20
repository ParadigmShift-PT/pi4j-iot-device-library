package pt.unl.fct.di.novasys.iot.device.analog;

import com.pi4j.context.Context;

public class AnalogInputDevice {

    private String name;
    private int ID;
    private int line;

    private GroveBaseHatADC hat;

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

    public int readRaw() { return hat.readRaw(this.line); }

    public int readVoltage() { return hat.readVoltage(this.line); }

    public int read() { return hat.readRatio(this.line); }

    public String getName() { return name; }

    public int getID() { return ID; }

    public int getLine() { return line; }

    public GroveBaseHatADC getHat() { return hat; }
}
