/********************************************************************************************
 * Grove3AxisAccelerometer
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
 * Driver for the Grove 3-Axis Digital Accelerometer (MMA7660FC). Wakes the
 * MMA7660 into active mode at 32 samples/sec and exposes raw axis readings,
 * acceleration in g, and a richer {@link AccelData} record with looked-up
 * angle data (the chip reports values from 0–63 which are pre-mapped to
 * angles in {@link AccelLookup}, including a "tilt sentinel" value of 255
 * for indices 22–42 where the chip cannot report a meaningful angle).
 *
 * <p>The chip is on I²C address {@code 0x4C} and is opened on bus 1.
 */
public class Grove3AxisAccelerometer implements I2CDevice {
	
    public final static int MMA7660_ADDR = 0x4c;

    public final static int MMA7660_X = 0x00;
    public final static int MMA7660_Y = 0x01;
    public final static int MMA7660_Z = 0x02;
    public final static int MMA7660_TILT = 0x03;
    public final static int MMA7660_SRST = 0x04;
    public final static int MMA7660_SPCNT = 0x05;
    public final static int MMA7660_INTSU = 0x06;
    public final static int MMA7660_SHINTX = 0x80;
    public final static int MMA7660_SHINTY = 0x40;
    public final static int MMA7660_SHINTZ = 0x20;
    public final static int MMA7660_GINT = 0x10;
    public final static int MMA7660_ASINT = 0x08;
    public final static int MMA7660_PDINT = 0x04;
    public final static int MMA7660_PLINT = 0x02;
    public final static int MMA7660_FBINT = 0x01;
    public final static int MMA7660_MODE = 0x07;
    public final static int MMA7660_STAND_BY = 0x00;
    public final static int MMA7660_ACTIVE = 0x01;
    public final static int MMA7660_SR = 0x08;     // sample rate register
    public final static int AUTO_SLEEP_120 = 0X00; // 120 sample per second
    public final static int AUTO_SLEEP_64 = 0X01;
    public final static int AUTO_SLEEP_32 = 0X02;
    public final static int AUTO_SLEEP_16 = 0X03;
    public final static int AUTO_SLEEP_8 = 0X04;
    public final static int AUTO_SLEEP_4 = 0X05;
    public final static int AUTO_SLEEP_2 = 0X06;
    public final static int AUTO_SLEEP_1 = 0X07;
    public final static int MMA7660_PDET = 0x09;
    public final static int MMA7660_PD = 0x0A;
    public final static int MMA7660_TIMEOUT = 500;

    private final I2C accelerometer;

    private AccelLookup[] accLookup;

    /**
     * Opens the I²C handle, builds the lookup table, and puts the chip
     * into active mode at 32 Hz.
     *
     * @param pi4j Pi4J context
     * @throws IOException if the chip cannot be initialised
     */
    public Grove3AxisAccelerometer(Context pi4j) throws IOException {
        I2CConfigBuilder configtext = I2C.newConfigBuilder(pi4j);
        configtext.id("Grovepi-plus" + MMA7660_ADDR);
        configtext.name("My I2C Bus " + MMA7660_ADDR);
        configtext.bus(GrovePi4J.I2C_BUS);
        configtext.device(MMA7660_ADDR);
        I2CConfig c = configtext.build();
        accelerometer = pi4j.create(c);

        this.accLookup = new AccelLookup[64];
        for (int i = 0; i < accLookup.length; i++) {
            accLookup[i] = new AccelLookup();
        }
        this.init();
    }

    private void setMode(int mode) {
        byte[] cmd = {MMA7660_MODE, (byte)mode};
        accelerometer.write(cmd);
    }

    private void setSampleRate(int rate) {
        byte[] cmd = {MMA7660_SR, (byte)rate};
        accelerometer.write(cmd);
    }

    private void initAccelTable() {
        int i;
        double val, valZ;

        for (i = 0, val = 0; i < 32; i++) {
            accLookup[i].g = (float)val;
            val += 0.047;
        }

        for (i = 63, val = -0.047; i > 31; i--) {
            accLookup[i].g = (float)val;
            val -= 0.047;
        }

        for (i = 0, val = 0, valZ = 90; i < 22; i++) {
            accLookup[i].xyAngle = (float)val;
            accLookup[i].zAngle = (float)valZ;

            val += 2.69;
            valZ -= 2.69;
        }

        for (i = 63, val = -2.69, valZ = -87.31; i > 42; i--) {
            accLookup[i].xyAngle = (float)val;
            accLookup[i].zAngle = (float)valZ;

            val -= 2.69;
            valZ += 2.69;
        }

        for (i = 22; i < 43; i++) {
            accLookup[i].xyAngle = 255;
            accLookup[i].zAngle = 255;
        }
    }

    protected void init() throws IOException {
        initAccelTable();
        setMode(MMA7660_STAND_BY);
        setSampleRate(AUTO_SLEEP_32);
        setMode(MMA7660_ACTIVE);
    }

    /**
     * Reads the raw signed axis values (X, Y, Z), busy-retrying through
     * the chip's "alert" bit until a clean reading is obtained, with a
     * {@link #MMA7660_TIMEOUT}-microsecond cap.
     *
     * @return a 3-element array of raw axis values in 6-bit two's
     *         complement (range −32..31)
     */
    public int[] getXYZ() {
        byte[] val = new byte[3];
        int count = 0;

        boolean reset = true, done = false;
        long start = System.nanoTime();
        while (!done) { // imagine this is a label like the arduino code START:
            if (reset) {
                count = 0;
                val[0] = val[1] = val[2] = 64;
                start = System.nanoTime();
                reset = false;
            }

            while (true) {
                if (count < 3) {
                    while (val[count] > 63) {
                        accelerometer.read(val);

                        if ((System.nanoTime() - start) / 1000 >
                            MMA7660_TIMEOUT) {
                            reset = true; // and this
                            break;
                        }
                    }
                    if (reset) // plus this .. is a goto START ——— I hate Java
                        break;
                } else {
                    done = true;
                    break;
                }
                count++;
            }
        }

        int[] xyz = new int[3];
        xyz[0] = ((byte)(val[0] << 2)) / 4;
        xyz[1] = ((byte)(val[1] << 2)) / 4;
        xyz[2] = ((byte)(val[2] << 2)) / 4;

        return xyz;
    }

    // simpler version with no reset mechanism
    /* public int[] getXYZ() {
        byte[] val = new byte[3];

        int count = 0;
        val[0] = val[1] = val[2] = 64;

        while (true) {
            if (count < 3) {
                while (val[count] > 63) {
                    accelerometer.read(val);
                }
            } else {
                break;
            }
            count++;
        }

        int[] xyz = new int[3];
        xyz[0] = ((byte) (val[0] << 2)) / 4;
        xyz[1] = ((byte) (val[1] << 2)) / 4;
        xyz[2] = ((byte) (val[2] << 2)) / 4;

        return xyz;
    } */

    /**
     * Reads acceleration in g (1 g ≈ 9.81 m/s²). Each axis is the raw
     * {@link #getXYZ()} value divided by 21 (the MMA7660's
     * counts-per-g constant in 1.5 g full-scale mode).
     *
     * @return a 3-element array of accelerations in g
     */
    public float[] getAcceleration() {
        float[] a_xyz = new float[3];
        int[] xyz = getXYZ();

        a_xyz[0] = (float)xyz[0] / 21;
        a_xyz[1] = (float)xyz[1] / 21;
        a_xyz[2] = (float)xyz[2] / 21;

        return a_xyz;
    }

    /**
     * Reads the chip and returns the looked-up {@link AccelData} record
     * with per-axis g and angle values.
     *
     * @return acceleration plus angle data for each axis
     */
    public AccelData getAccelerationData() {
        byte[] val = new byte[3];
        int count;
        boolean error;

        // long start = System.nanoTime(); // TODO it always times out so not using it
        do {
            error = false;
            count = 0;

            while (true) {
                if (count < 3) {
                    accelerometer.read(val);
                    if ((0x40 & val[count]) == 0x40) {
                        error = true;
                        break;
                    }
                } else {
                    break;
                }
                count++;
            }
            // if ((System.nanoTime() - start) / 1000 > MMA7660_TIMEOUT) {
            // System.out.println("Timed out reading acceleration");
            // }
        } while (error);

        AccelLookup x, y, z;
        x = accLookup[val[0] & 0x3F];
        y = accLookup[val[1] & 0x3F];
        z = accLookup[val[2] & 0x3F];
        return new AccelData(x, y, z);
    }

    /**
     * Bulk-reads the first 11 chip registers and exposes them as an
     * {@link MMA7660Data} record. Useful when debugging the chip's tilt,
     * sample-count, interrupt-source, mode, sample-rate, and pulse-detect
     * registers in one go.
     *
     * @return all 11 register values, unsigned
     */
    public MMA7660Data getAllData() {
        byte[] data_buf = new byte[11];

        accelerometer.read(data_buf);

        MMA7660Data data = new MMA7660Data(
            data_buf[0] & 0xFF,   // x
            data_buf[1] & 0xFF,   // y
            data_buf[2] & 0xFF,   // z
            data_buf[3] & 0xFF,   // tilt
            data_buf[4] & 0xFF,   // srst
            data_buf[5] & 0XFF,   // spcnt
            data_buf[6] & 0XFF,   // intsu
            data_buf[7] & 0XFF,   // mode
            data_buf[8] & 0XFF,   // sr
            data_buf[9] & 0XFF,   // pdet
            data_buf[10] & 0XFF   // pd
        );

        return data;
    }

    /** Closes the underlying I²C handle. */
    public void close() { accelerometer.close(); }

    private class AccelLookup {
        public float g; // these are public to make it easier to init the accel table
        public float xyAngle;
        public float zAngle;

        AccelLookup() {}

        @Override
        public String toString() {
            return String.format("g= %f, xyAngle= %f, zAngle= %f", g, xyAngle,
                                 zAngle);
        }
    }

    /**
     * One reading from the accelerometer expressed as g and angle data
     * for each axis. {@code xyAngle} of 255 means the chip cannot report
     * a meaningful angle (the value falls in the chip's tilt-sentinel
     * range).
     *
     * @param x X-axis lookup
     * @param y Y-axis lookup
     * @param z Z-axis lookup
     */
    public record AccelData(AccelLookup x, AccelLookup y, AccelLookup z) {
        @Override
        public String toString() {
            return String.format("x: %s\ny: %s\nz: %s", x, y, z);
        }
    }

    /**
     * Snapshot of the first 11 MMA7660 registers (all unsigned).
     *
     * @param x     X axis (XOUT)
     * @param y     Y axis (YOUT)
     * @param z     Z axis (ZOUT)
     * @param tilt  TILT register
     * @param srst  sample-rate-status register
     * @param spcnt sleep-count register
     * @param intsu interrupt-source register
     * @param mode  mode register
     * @param sr    sample-rate register
     * @param pdet  pulse-detect register
     * @param pd    pulse-detect parameters register
     */
    public record MMA7660Data(int x, int y, int z, int tilt, int srst, int spcnt,
                    int intsu, int mode, int sr, int pdet, int pd){

    }
}
