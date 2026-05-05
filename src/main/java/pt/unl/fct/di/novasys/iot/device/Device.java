package pt.unl.fct.di.novasys.iot.device;

/**
 * Marker interface for every device class in the library. All device types
 * — {@link DigitalDevice}, {@link I2CDevice}, and the analogue / serial
 * device wrappers — implement this interface, so callers can hold a generic
 * reference (e.g., for a registry of attached peripherals) without coupling
 * to a specific bus.
 */
public interface Device {

}
