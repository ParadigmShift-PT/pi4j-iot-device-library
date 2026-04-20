package pt.unl.fct.di.novasys.iot.device;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;

public abstract class DigitalDevice implements Device {

    protected final String name;
    protected final int ID;
    protected int line;
    protected final Context pi4j;
    protected DigitalInput dataIn;
    protected DigitalInput dataIn2;
    protected DigitalOutput dataOut;
    protected DigitalOutput clk;
    protected DigitalInputConfig dataInCfg;
    protected DigitalOutputConfig dataOutCfg;
    protected DigitalOutputConfig clkCfg;

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

    public String getName() { return this.name; }

    public int getID() { return this.ID; }

    public int getLineNumber() { return this.line; };

    protected void delayMicroseconds(int us) {
        long waitUntil = System.nanoTime() + (us * 1_000);
        while (waitUntil > System.nanoTime())
            ;
    }

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

    public DigitalOutput getDataOutputPin() { return this.dataOut; }

    public DigitalInput getDataInputPin() { return this.dataIn; }

    public DigitalOutput getClockOutputPin() { return this.clk; }
}
