package pt.unl.fct.di.novasys.iot.device;

/**
 * Marker interface for devices attached via the I²C bus. Implementations
 * (e.g., {@code GroveRgbLcd}, {@code Grove3AxisAccelerometer}) own their own
 * Pi4J {@code I2C} handle and expose a higher-level API on top of it; this
 * interface lets callers identify I²C peripherals without depending on a
 * concrete class.
 */
public interface I2CDevice extends Device {

}
