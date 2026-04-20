package pt.unl.fct.di.novasys.iot.device.i2c;

import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CConfigBuilder;
import java.io.IOException;
import pt.unl.fct.di.novasys.iot.device.I2CDevice;

public class GroveBarometer implements I2CDevice {

    public final static int STANDARD_SEA_LEVEL = 101325; // in Pa

    private final static int BMP280_ADDRESS = 0x77;

    private final static int BMP280_REG_DIG_T1 = 0x88;
    private final static int BMP280_REG_DIG_T2 = 0x8A;
    private final static int BMP280_REG_DIG_T3 = 0x8C;
    private final static int BMP280_REG_DIG_P1 = 0x8E;
    private final static int BMP280_REG_DIG_P2 = 0x90;
    private final static int BMP280_REG_DIG_P3 = 0x92;
    private final static int BMP280_REG_DIG_P4 = 0x94;
    private final static int BMP280_REG_DIG_P5 = 0x96;
    private final static int BMP280_REG_DIG_P6 = 0x98;
    private final static int BMP280_REG_DIG_P7 = 0x9A;
    private final static int BMP280_REG_DIG_P8 = 0x9C;
    private final static int BMP280_REG_DIG_P9 = 0x9E;

    private final static int BMP280_REG_CHIPID = 0xD0;
    private final static int BMP280_REG_VERSION = 0xD1;
    private final static int BMP280_REG_SOFTRESET = 0xE0;

    private final static int BMP280_REG_CONTROL = 0xF4;
    private final static int BMP280_REG_CONFIG = 0xF5;
    private final static int BMP280_REG_PRESSUREDATA = 0xF7;
    private final static int BMP280_REG_TEMPDATA = 0xFA;

    private final I2C barometer;

    private boolean transportOK;

    private int dig_T1;   // uint16_t
    private short dig_T2; // int16_t
    private short dig_T3; // int16_t
    private int dig_P1;   // uint16_t
    private short dig_P2; // int16_t
    private short dig_P3; // int16_t
    private short dig_P4; // int16_t
    private short dig_P5; // int16_t
    private short dig_P6; // int16_t
    private short dig_P7; // int16_t
    private short dig_P8; // int16_t
    private short dig_P9; // int16_t
    private int t_fine;   // int32_t

    public GroveBarometer(Context pi4j) throws IOException {
        I2CConfigBuilder configtext = I2C.newConfigBuilder(pi4j);
        configtext.id("Grovepi-plus" + BMP280_ADDRESS);
        configtext.name("My I2C Bus " + BMP280_ADDRESS);
        configtext.bus(GrovePi4J.I2C_BUS);
        configtext.device(BMP280_ADDRESS);
        I2CConfig c = configtext.build();
        barometer = pi4j.create(c);

        this.init();
    }

    private void init() {
        this.dig_T1 = read16LE(BMP280_REG_DIG_T1);
        this.dig_T2 = (short)read16LE(BMP280_REG_DIG_T2);
        this.dig_T3 = (short)read16LE(BMP280_REG_DIG_T3);
        this.dig_P1 = read16LE(BMP280_REG_DIG_P1);
        this.dig_P2 = (short)read16LE(BMP280_REG_DIG_P2);
        this.dig_P3 = (short)read16LE(BMP280_REG_DIG_P3);
        this.dig_P4 = (short)read16LE(BMP280_REG_DIG_P4);
        this.dig_P5 = (short)read16LE(BMP280_REG_DIG_P5);
        this.dig_P6 = (short)read16LE(BMP280_REG_DIG_P6);
        this.dig_P7 = (short)read16LE(BMP280_REG_DIG_P7);
        this.dig_P8 = (short)read16LE(BMP280_REG_DIG_P8);
        this.dig_P9 = (short)read16LE(BMP280_REG_DIG_P9);

        barometer.writeRegister(BMP280_REG_CONTROL, 0x3F);
    }

    private byte read8(int reg) { return (byte)barometer.readRegister(reg); }

    private int read16(int reg) { return barometer.readRegisterWord(reg); }

    private int read16LE(int reg) {
        int word = barometer.readRegisterWord(reg);

        return ((word & 0xFF) << 8) | ((word >> 8) & 0xFF);
    }

    private int read24(int reg) {
        byte[] buf = barometer.readRegisterNBytes(reg, 3);

        int data = (buf[0] & 0xFF);
        data <<= 8;
        data |= (buf[1] & 0xFF);
        data <<= 8;
        data |= (buf[2] & 0xFF);

        return data;
    }

    public double getTemperature() {
        int var1, var2;
        int adcT = read24(BMP280_REG_TEMPDATA);

        adcT >>= 4;

        var1 = (((adcT >> 3) - ((int)(dig_T1 << 1))) * ((int)dig_T2)) >> 11;
        var2 =
            (((((adcT >> 4) - ((int)dig_T1)) * ((adcT >> 4) - ((int)dig_T1))) >>
              12) *
             ((int)dig_T3)) >>
            14;

        t_fine = var1 + var2;
        double T = (t_fine * 5 + 128) >> 8;
        return T / 100;
    }

    public long getPressure() {
        long var1, var2, p;
        getTemperature();

        int adcP = read24(BMP280_REG_PRESSUREDATA);

        adcP >>= 4;
        var1 = ((long)t_fine) - 128000;
        var2 = var1 * var1 * (long)dig_P6;
        var2 = var2 + ((var1 * (long)dig_P5) << 17);
        var2 = var2 + (((long)dig_P4) << 35);
        var1 =
            ((var1 * var1 * (long)dig_P3) >> 8) + ((var1 * (long)dig_P2) << 12);
        var1 = (((((long)1) << 47) + var1)) * ((long)dig_P1) >> 33;
        if (var1 == 0) {
            return 0;
        }
        p = 1048576 - adcP;
        p = (((p << 31) - var2) * 3125) / var1;
        var1 = (((long)dig_P9) * (p >> 13) * (p >> 13)) >> 25;
        var2 = (((long)dig_P8) * p) >> 19;
        p = ((p + var1 + var2) >> 8) + (((long)dig_P7) << 4);

        long result = (p / 256) & 0xFFFFFFFFL;
        return result;
    }

    public double calcAltitude() {
        double t = getTemperature();
        double p1 = getPressure();
        return calcAltitude(STANDARD_SEA_LEVEL, p1, t);
    }

    public double calcAltitude(double p0) {
        double t = getTemperature();
        double p1 = getPressure();
        return calcAltitude(p0, p1, t);
    }

    public double calcAltitude(double p0, double p1, double t) {
        double C;
        C = (p0 / p1);
        C = Math.pow(C, (1.0 / 5.25588)) - 1.0;
        C = (C * (t + 273.15)) / 0.0065;
        return C;
    }

    public BarometerData getBarometerData(double pressure) {
        return new BarometerData(getTemperature(), getPressure(),
                                 calcAltitude(pressure));
    }

    public BarometerData getBarometerData() {
        return new BarometerData(getTemperature(), getPressure(),
                                 calcAltitude());
    }

    public record BarometerData(double temperature, long pressure,
                                double altitude) {
        @Override
        public String toString() {
            return String.format(
                "Temperature (ºC): %.2f\nPressure (Pa): %d\nAltitude (m): %.2f",
                temperature, pressure, altitude);
        }
    }
}
