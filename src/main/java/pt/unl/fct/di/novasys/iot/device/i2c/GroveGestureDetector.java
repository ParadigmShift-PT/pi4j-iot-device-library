/********************************************************************************************
 * GroveGestureDetector
 *
 * @author João Brilha (j.brilha@campus.fct.unl.pt)
 * @author João Leitão (jc.leitao@fct.unl.pt)
 ********************************************************************************************/

package pt.unl.fct.di.novasys.iot.device.i2c;

import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CConfigBuilder;

import pt.unl.fct.di.novasys.iot.device.I2CDevice;

import java.io.IOException;

public class GroveGestureDetector implements I2CDevice {
	
    public final static int PAJ7620_ADDR = 0x73;

    public final static byte PAJ7620_REG_BANK_SEL = (byte)0xEF;
    public final static byte PAJ7620_REG_RESULT_L = (byte)0x43;
    public final static byte PAJ7620_REG_RESULT_H = (byte)0x44;

    public final static int PAJ7620_GESTURE_COUNT = 9;

    public static enum PAJ7620GestureType {
        UP(0),
        DOWN(1),
        LEFT(2),
        RIGHT(3),
        PUSH(4),
        PULL(5),
        CLOCKWISE(6),
        COUNTER_CLOCKWISE(7),
        WAVE(8);

        public final int code;

        PAJ7620GestureType(int code) { this.code = code; }
    }

    public static enum PAJ7620ReportMode {
        FAR_240FPS(0),
        FAR_120FPS(1),
        NEAR_240FPS(2),
        NEAR_120FPS(3);

        public final int code;

        PAJ7620ReportMode(int code) { this.code = code; }
    }

    private static final int[][] initRegister = {
        // BANK 0
        {0xEF,0x00}, {0x37,0x07}, {0x38,0x17}, {0x39,0x06}, {0x42,0x01}, 
        {0x46,0x2D}, {0x47,0x0F}, {0x48,0x3C}, {0x49,0x00}, {0x4A,0x1E}, 
        {0x4C,0x20}, {0x51,0x10}, {0x5E,0x10}, {0x60,0x27}, {0x80,0x42}, 
        {0x81,0x44}, {0x82,0x04}, {0x8B,0x01}, {0x90,0x06}, {0x95,0x0A}, 
        {0x96,0x0C}, {0x97,0x05}, {0x9A,0x14}, {0x9C,0x3F}, {0xA5,0x19}, 
        {0xCC,0x19}, {0xCD,0x0B}, {0xCE,0x13}, {0xCF,0x64}, {0xD0,0x21}, 
        // BANK 1
        {0xEF,0x01}, {0x02,0x0F}, {0x03,0x10}, {0x04,0x02}, {0x25,0x01},
        {0x27,0x39}, {0x28,0x7F}, {0x29,0x08}, {0x3E,0xFF}, {0x5E,0x3D}, 
        {0x65,0x96}, {0x67,0x97}, {0x69,0xCD}, {0x6A,0x01}, {0x6D,0x2C}, 
        {0x6E,0x01}, {0x72,0x01}, {0x73,0x35}, {0x77,0x01}, {0xEF,0x00},
    };

    private final I2C detector;

    public GroveGestureDetector(Context pi4j) throws IOException {
        I2CConfigBuilder configtext = I2C.newConfigBuilder(pi4j);
        configtext.id("Grovepi-plus" + PAJ7620_ADDR);
        configtext.name("My I2C Bus " + PAJ7620_ADDR);
        configtext.bus(GrovePi4J.I2C_BUS);
        configtext.device(PAJ7620_ADDR);
        I2CConfig c = configtext.build();
        detector = pi4j.create(c);
        this.init();
    }

    protected void init() throws IOException {
        byte[] cmd = { (byte) 0xFF, (byte) 0x00 };
        detector.write(cmd);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if ((detector.readRegister(0x01) != 0x76) || (detector.readRegister(0x00) != 0x20)) {
            throw new IOException("Invalid values in the registers");
        }
        for (int i = 0; i < initRegister.length; i++) {
            byte[] cmd1 = { (byte) initRegister[i][0], (byte) initRegister[i][1] };
            detector.write(cmd1);

        }

        // the arduino code inits this to NEAR_240FPS but I found that kind of meh
        setReportMode(PAJ7620ReportMode.FAR_240FPS);
    }

    public PAJ7620GestureType getGesture() {
        PAJ7620GestureType gesture = null;

        int code = (detector.readRegister(PAJ7620_REG_RESULT_H) << 8)
                + detector.readRegister(PAJ7620_REG_RESULT_L);
        if (code == 0)
            return null;

        for (int i = PAJ7620GestureType.UP.code; i < PAJ7620_GESTURE_COUNT; i++) {
            if (code == (1 << i)) {
                gesture= PAJ7620GestureType.values()[i];
                detector.readRegister(PAJ7620_REG_RESULT_H);
                detector.readRegister(PAJ7620_REG_RESULT_L);
                break;
            }
        }

        return gesture;
    }

    protected boolean setReportMode(PAJ7620ReportMode mode) {
        int idleTime = 0;
        byte[] cmd = { PAJ7620_REG_BANK_SEL, 1 };
        detector.write(cmd);

        switch (mode) {
            // Far Mode: 1 report time = (77 + R_IDLE_TIME) * T
            case FAR_240FPS:
                idleTime = 53; // 1/(240*T) - 77
                break;
            case FAR_120FPS:
                idleTime = 183; // 1/(120*T) - 77
                break;
            // Near Mode: 1 report time = (112 + R_IDLE_TIME) * T
            case NEAR_240FPS:
                idleTime = 18; // 1/(240*T) - 112
                break;
            case NEAR_120FPS:
                idleTime = 148; // 1/(120*T) - 112
                break;
            default:
                return false;
        }

        byte[] cmd1 = { 0x65, (byte)(idleTime & 0xFF) };
        detector.write(cmd1);
        byte[] cmd2 = { PAJ7620_REG_BANK_SEL, 0 };
        detector.write(cmd2);

        return true;
    }
}

