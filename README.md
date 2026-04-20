# Pi4J IoT Device Library

## Authors

- João Brilha (j.brilha@campus.fct.unl.pt)
- João Leitão (jc.leitao@fct.unl.pt)

## Description

This is a Device library for Raspberry based on the Pi4J project.

The library currently is focused on supporting Grove Devices. It has multiple Java Objects that represent the different devices and can be used freely to interact with these devices from a Java environment.

The library also relies on some abstractions provided by the yafna project grovepi-pi4j.

A list of supported devices is present in `supported_devices.md`.

## How to use

### Dependencies

Copy and paste the following block inside your `pom.xml dependencies` block.

```
<dependency>
	<groupId>pt.unl.fct.di.novasys.iot</groupId>
	<artifactId>pi4j-iot-device-library</artifactId>
	<version>[0.0.1,)</version>
</dependency>
```

### Repository Setup

If you haven't already done so, you will need to add the following to your `pom.xml` file.

```
<repositories>
    <repository>
        <id>novasys-mvn</id>
        <url>https://novasys.di.fct.unl.pt/packages/mvn</url>
    </repository>
</repositories>
```

## License

To-Be-Defined (soon)
