# Pi4J IoT Device Library

A high-level catalogue of Java device wrappers for Grove sensors and actuators connected to a Raspberry Pi. The library hides the protocol details (GPIO, I²C, serial) behind simple read / write APIs and exposes a consistent device hierarchy: every concrete device extends `DigitalDevice`, an `AnalogInputDevice`, an `I2CDevice`, or wraps a serial port.

**Group ID:** `pt.paradigmshift.iot`
**Artifact ID:** `pi4j-iot-device-library`
**Current version:** `1.0.0`
**Tested on:** Raspberry Pi 4 and 5 with Grove sensors / actuators (generic kits work too as long as pin assignments match).

---

## Origin

This library is a fork of the IoT device library originally developed at
[NOVA School of Science and Technology (NOVA FCT)](https://www.fct.unl.pt)
as part of the [TaRDIS](https://tardis-project.eu) European research project
on swarm systems (work package 6):

> **Original repository:**
> https://codelab.fct.unl.pt/di/research/tardis/wp6/iot/protocols/pi4j-iot-device-library
>
> **Original authors:** João Brilha, João Leitão

The fork was created to serve as the IoT device layer used by the StoneFlux
edge gateway and is maintained by [ParadigmShift](https://www.paradigmshift.pt).
All original authorship is acknowledged and preserved. Additions and
modifications made after the fork are copyright ParadigmShift.

---

## Supported devices

See [`supported_devices.md`](supported_devices.md) for the full list. Summary:

| Bus | Inputs | Outputs |
|---|---|---|
| **I²C** | 3-Axis Accelerometer, Gesture Detector, Barometer, I2C Hub 8 | LCD (16×2), RGB LCD, LED Matrix, 4-Digit Display |
| **Digital** | Button, Tilt Switch, PIR Motion, Flame, Touch, Line Finder, Encoder, Ultrasonic Ranger | Vibration Motor, LED, Buzzer, Chainable RGB, LED Bar |
| **Analog** | Gas (MQ2), Loudness, Light, GSR (via Grove Base Hat ADC) | — |
| **Serial** | GPS (Air530, Pi 4 only — no Pi 5 serial provider in upstream Pi4J yet) | — |

Generic `DigitalInputDevice`, `DigitalOutputDevice`, and `AnalogInputDevice` classes are also available so you can wire a Grove peripheral that isn't yet wrapped.

---

## Usage

Add to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>paradigmshift-repository</id>
        <name>ParadigmShift Repository</name>
        <url>https://maven.paradigmshift.pt/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>pt.paradigmshift.iot</groupId>
        <artifactId>pi4j-iot-device-library</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

`pi4j-iot-device-library` brings in `grovepi-pi4j` and `pi4j-components` transitively, which in turn pull `pi4j-core` plus the standard Pi4J runtime plugins. This single dependency is enough to drive any supported peripheral.

### Reading a digital input

```java
Context pi4j = Pi4J.newAutoContext();

DigitalInputDevice button =
    new DigitalInputDevice(pi4j, "button", /* pin */ 26, /* deviceID */ 1);

if (button.isHigh()) {
    // pressed
}
```

### Driving a digital output

```java
DigitalOutputDevice led =
    new DigitalOutputDevice(pi4j, "led", /* pin */ 17, /* deviceID */ 2);

led.setHigh();
// ... later
led.setLow();
```

### Reading an analogue sensor through the Grove Base Hat

```java
// First analogue read on a process initialises the shared GroveBaseHatADC.
AnalogInputDevice light =
    new AnalogInputDevice(pi4j, "lightSensor", /* analog port */ 0, /* deviceID */ 3);

int raw = light.readRaw();           // 0 – 4095
int millivolts = light.readVoltage();
int ratio = light.read();            // 0 – 1000
```

### Showing text on a Grove RGB LCD (I²C)

```java
GroveRgbLcd lcd = new GroveRgbLcd(pi4j, /* deviceID */ 4);
lcd.setRGB(0, 128, 255);
lcd.setText("Hello, StoneFlux");
```

> **Hardware note:** the library must run on a Raspberry Pi with the standard Pi4J runtime providers configured (`raspberrypi`, `linuxfs`, `gpiod` by default). It will compile on any platform but will fail at runtime elsewhere because of the underlying native libraries.

---

## Building

Requires Java 17 and Maven 3.6+.

```bash
mvn verify    # compile + (no tests yet)
mvn package   # produces JAR, sources JAR, and Javadoc JAR
mvn deploy    # publish to maven.paradigmshift.pt (requires REPOSILITE_TOKEN)
```

This artifact depends on `pt.paradigmshift.iot:grovepi-pi4j:0.5.0` and `pt.paradigmshift.iot:pi4j-components:0.0.7`. Those must be available either in `~/.m2/` (after a local `mvn install` of each) or on `maven.paradigmshift.pt`.

## Releasing

Push a version tag — the GitHub Actions CI workflow builds and deploys automatically:

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## License

Copyright (c) 2026 ParadigmShift, Lda. See [LICENSE](LICENSE) for full terms.

Commercial use outside of ParadigmShift requires a written licence.
Contact: [info@paradigmshift.pt](mailto:info@paradigmshift.pt)
