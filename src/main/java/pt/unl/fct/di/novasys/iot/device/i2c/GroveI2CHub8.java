package pt.unl.fct.di.novasys.iot.device.i2c;

import com.github.yafna.raspberry.grovepi.pi4j.GrovePi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import java.io.IOException;
import pt.unl.fct.di.novasys.iot.device.I2CDevice;

public class GroveI2CHub8 implements I2CDevice {

    private final static int TCA9548_ADDRESS = 0x70;

    public enum TCAChannel {
        _0(0x1),
        _1(0x2),
        _2(0x4),
        _3(0x8),
        _4(0x10),
        _5(0x20),
        _6(0x40),
        _7(0x80);

        private final int value;

        TCAChannel(int value) { this.value = value; }

        public int getValue() { return value; }
    }

    private final I2C hub;

    private int channels; // uint16_t

    public GroveI2CHub8(Context pi4j) throws IOException {
        I2CConfig config = I2C.newConfigBuilder(pi4j)
                               .id("Grovepi-plus" + TCA9548_ADDRESS)
                               .name("My I2C Bus " + TCA9548_ADDRESS)
                               .bus(GrovePi4J.I2C_BUS)
                               .device(TCA9548_ADDRESS)
                               .build();

        this.hub = pi4j.create(config);

        this.init();
    }

    private void init() { closeAll(); }

    public void closeAll() {
        this.channels = 0x00;
        hub.write(channels);
    }

    public void openAll() {
        this.channels = 0xFF;
        hub.write(channels);
    }

    public void closeChannel(TCAChannel channel) {
        this.channels ^= channel.getValue();
        hub.write(channels);
    }

    public void openChannel(TCAChannel channel) {
        this.channels |= channel.getValue();
        hub.write(channels);
    }
}
