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

/**
 * Driver for the Grove Gesture Detector (PAJ7620U2). Recognises nine
 * coarse gestures (up/down/left/right swipe, push/pull, clockwise /
 * counter-clockwise rotation, wave) and exposes them as
 * {@link PAJ7620GestureType} values.
 *
 * <p>The chip is initialised with a long preset register sequence
 * (banks 0 and 1) and defaulted to {@link PAJ7620ReportMode#FAR_240FPS}.
 * Use {@link #getGesture()} to poll for the most recent gesture; the
 * method returns {@code null} when no gesture has occurred since the
 * previous call.
 *
 * <p>Lives at I²C address {@code 0x73} on bus 1.
 */
public class GroveGestureDetector implements I2CDevice {
	
    public final static int PAJ7620_ADDR = 0x73;

    public final static byte PAJ7620_REG_BANK_SEL = (byte)0xEF;
    public final static byte PAJ7620_REG_RESULT_L = (byte)0x43;
    public final static byte PAJ7620_REG_RESULT_H = (byte)0x44;

    public final static int PAJ7620_GESTURE_COUNT = 9;

    /** The nine gestures the PAJ7620 can report. */
    public static enum PAJ7620GestureType {
        /** Hand swipe upward. */ UP(0),
        /** Hand swipe downward. */ DOWN(1),
        /** Hand swipe leftward. */ LEFT(2),
        /** Hand swipe rightward. */ RIGHT(3),
        /** Hand pushed toward the sensor. */ PUSH(4),
        /** Hand pulled away from the sensor. */ PULL(5),
        /** Hand rotated clockwise above the sensor. */ CLOCKWISE(6),
        /** Hand rotated counter-clockwise above the sensor. */ COUNTER_CLOCKWISE(7),
        /** Hand wave (rapid back-and-forth). */ WAVE(8);

        /** Wire-format integer for this gesture (matches the chip's bit position). */
        public final int code;

        PAJ7620GestureType(int code) { this.code = code; }
    }

    /**
     * Sampling-rate / range presets. "Far" mode tolerates more distance
     * but loses sensitivity; "Near" mode is more sensitive but requires
     * the hand to be closer. Higher FPS picks up faster gestures at the
     * cost of higher chip activity.
     */
    public static enum PAJ7620ReportMode {
        /** Far range, 240 reports per second. */ FAR_240FPS(0),
        /** Far range, 120 reports per second. */ FAR_120FPS(1),
        /** Near range, 240 reports per second. */ NEAR_240FPS(2),
        /** Near range, 120 reports per second. */ NEAR_120FPS(3);

        /** Wire-format integer for this report mode. */
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

    /**
     * Opens the I²C handle and runs the PAJ7620 init sequence (bank
     * select, identity check, register preset, report mode default).
     *
     * @param pi4j Pi4J context
     * @throws IOException if the chip is missing or returns invalid
     *                     identity bytes
     */
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

    /**
     * Polls the chip's gesture-result register and decodes the latest
     * recognised gesture.
     *
     * @return the most recent gesture, or {@code null} if no gesture has
     *         been recognised since the last call
     */
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

