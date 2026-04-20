package pt.unl.fct.di.novasys.iot.device.analog;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;

public class GroveBaseHatADC {
    private final I2C i2c;
    private static GroveBaseHatADC instance;

    private GroveBaseHatADC(Context pi4j, int address) {
        this.i2c =
            pi4j.create(I2C.newConfigBuilder(pi4j)
                            .id("grove-hat-adc")
                            .name("Grove Base Hat ADC")
                            .bus(1)
                            .device(address) // 0x04 for STM32, 0x08 for MM32
                            .build());
    }

    public static void initialize(Context pi4j, int address) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        instance = new GroveBaseHatADC(pi4j, address);
    }

    public static GroveBaseHatADC getInstance(Context pi4j, int address) {
        if (instance == null) {
            initialize(pi4j, address);
        }
        return instance;
    }

    public static GroveBaseHatADC getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "Must call initialize(Context pi4j, int address) first or "
                + "use getInstance(Context pi4j, int address)");
        }
        return instance;
    }

    public int readRaw(int channel) {
        int addr = 0x10 + channel;
        return readRegister(addr);
    }

    public int readVoltage(int channel) {
        int addr = 0x20 + channel;
        return readRegister(addr);
    }

    // ratio between channel input voltage and power voltage in 0.1%
    public int readRatio(int channel) {
        int addr = 0x30 + channel;
        return readRegister(addr);
    }

    private int readRegister(int register) {
        int word = i2c.readRegisterWord(register);

        // swap bytes to little-endian
        return ((word & 0xFF) << 8) | ((word >> 8) & 0xFF);
    }
}
