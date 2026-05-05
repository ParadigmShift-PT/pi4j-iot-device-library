package pt.unl.fct.di.novasys.iot.device.serial;

import com.pi4j.context.Context;
import com.pi4j.io.serial.Serial;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Driver for the Grove GPS Air530 module. Opens the Pi's serial port
 * ({@code /dev/serial0}) at 9600 N81, runs a background reader thread
 * that parses NMEA 0183 sentences (GGA, GLL, GSA, GSV, RMC, VTG, ZDA,
 * TXT) and feeds them into a thread-safe {@link GPSData} object the
 * caller can poll at any time.
 *
 * <p>Only the Pi 4 is supported by upstream Pi4J for serial I/O — the
 * Pi 5 lacks a serial provider in the version this library is pinned to.
 *
 * <p>Lifecycle: construct → {@link #start()} → poll
 * {@link #getGPSData()} → {@link #stop()}.
 */
public class GroveGPSAir530 {
    private final Serial serial;

    private GPSData data;

    private volatile boolean running = false;
    private Thread reader;

    /**
     * Opens the serial port handle. Does not start reading — call
     * {@link #start()} for that.
     *
     * @param pi4j Pi4J context
     */
    public GroveGPSAir530(Context pi4j) {
        var config = Serial.newConfigBuilder(pi4j)
                         .device("/dev/serial0")
                         .use_9600_N81()
                         .build();

        this.serial = pi4j.create(config);
        this.data = new GPSData();
    }

    /**
     * Opens the serial port and starts the background reader thread.
     * Subsequent calls to {@link #getGPSData()} will reflect the latest
     * parsed sentence.
     */
    public void start() {
        serial.open();
        running = true;
        reader = new Thread(this::readSerial);
        reader.start();
    }

    /**
     * Signals the reader thread to terminate, interrupts any pending
     * I/O wait, and closes the serial port.
     */
    public void stop() {
        running = false;
        if (reader != null) {
            reader.interrupt();
        }
        serial.close();
    }

    private void readSerial() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                serial.getInputStream(), StandardCharsets.UTF_8));

            String line = "";
            int ch;

            while (running && !Thread.currentThread().isInterrupted()) {
                if (serial.available() > 0) {
                    ch = reader.read();

                    if (ch == '\n' || ch == '\r') {
                        if (!line.isEmpty()) {
                            parseLine(line.trim());
                        }
                        line = "";
                    } else if (ch != -1) {
                        line += (char)ch;
                    }
                }
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // https://en.wikipedia.org/wiki/NMEA_0183#NMEA_sentence_format
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-overview.html
    private void parseLine(String line) {
        try {
            if (line.startsWith("$GNGGA") || line.startsWith("$GPGGA")) {
                parseGGA(line);
            } else if (line.startsWith("$GNGLL") || line.startsWith("$GPGLL")) {
                parseGLL(line);
            } else if (line.startsWith("$GNGSA") || line.startsWith("$GPGSA")) {
                parseGSA(line);
            } else if (line.startsWith("$GNGSV") || line.startsWith("$GPGSV") ||
                       line.startsWith("$BDGSV")) {
                parseGSV(line);
            } else if (line.startsWith("$GNRMC") || line.startsWith("$GPRMC")) {
                parseRMC(line);
            } else if (line.startsWith("$GNVTG") || line.startsWith("$GPVTG")) {
                parseVTG(line);
            } else if (line.startsWith("$GNZDA") || line.startsWith("$GPZDA")) {
                parseZDA(line);
            } else if (line.startsWith("$GPTXT")) {
                parseTXT(line);
            }
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            e.printStackTrace();
        }
    }

    // GGA — Time, position, and fix related data
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-gga.html
    private void parseGGA(String line) {
        String[] fields = line.split(",");
        if (fields.length >= 15) {
            String utc = fields[1];
            String lat = fields[2];
            String latDir = fields[3];
            String lon = fields[4];
            String lonDir = fields[5];
            String quality = fields[6];
            String numSats = fields[7];
            String hdop = fields[8];
            String alt = fields[9];

            double newLat = 0, newLon = 0;
            float newHdop = 0, newAlt = 0;
            boolean validCoords = false;
            int satsUsed = 0;
            String newFix = "No Fix";

            if (!lat.isEmpty() && !lon.isEmpty() && !quality.isEmpty() &&
                !quality.equals("0")) {
                newLat = convertToDecimal(lat, latDir);
                newLon = convertToDecimal(lon, lonDir);
                validCoords = true;

                if (!numSats.isEmpty()) {
                    satsUsed = Integer.parseInt(numSats);
                }
                if (!hdop.isEmpty()) {
                    newHdop = Float.parseFloat(hdop);
                }
                if (!alt.isEmpty()) {
                    newAlt = Float.parseFloat(alt);
                }

                switch (quality) {
                case "1":
                    newFix = "GPS Fix";
                    break;
                case "2":
                    newFix = "DGPS Fix";
                    break;
                case "3":
                    newFix = "PPS Fix";
                    break;
                case "4":
                    newFix = "RTK Fix";
                    break;
                case "5":
                    newFix = "RTK Float";
                    break;
                case "6":
                    newFix = "Dead Reckoning";
                    break;
                default:
                    newFix = "Unknown Fix";
                    break;
                }
            }

            synchronized (data) {
                data.setFixTime(utc);
                if (validCoords) {
                    data.setLatitude(newLat);
                    data.setLongitude(newLon);
                    data.setValidFix(true);
                    data.setSatellitesUsed(satsUsed);
                    data.setHdop(newHdop);
                    data.setAltitude(newAlt);
                    data.setFix(newFix);
                } else {
                    data.setValidFix(false);
                    data.setFix("No Fix");
                }
            }
        }
    }

    // GLL — Position data: position fix, time of position fix, and status
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-gll.html
    private void parseGLL(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 7) {
            String lat = parts[1];
            String latDir = parts[2];
            String lon = parts[3];
            String lonDir = parts[4];
            String utc = parts[5];
            String status = parts[6];

            double newLat = 0, newLon = 0;
            boolean validCoords = false;

            if (status.equals("A") && !lat.isEmpty() && !lon.isEmpty()) {
                newLat = convertToDecimal(lat, latDir);
                newLon = convertToDecimal(lon, lonDir);
                validCoords = true;
            }

            synchronized (data) {
                data.setFixTime(utc);
                if (validCoords) {
                    data.setLatitude(newLat);
                    data.setLongitude(newLon);
                    data.setValidFix(true);
                }
            }
        }
    }

    // GSA — GPS DOP and active satellites
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-gsa.html
    private void parseGSA(String line) {
        String[] fields = line.split(",");
        if (fields.length >= 18) {
            String fix = fields[2];
            String hdop = fields[16];

            int satsUsed = 0;
            for (int i = 3; i <= 14; i++) {
                if (!fields[i].isEmpty()) {
                    satsUsed++;
                }
            }

            float newHdop = 0;
            if (!hdop.isEmpty()) {
                newHdop = Float.parseFloat(hdop);
            }

            String newFix = "No Fix";
            switch (fix) {
            case "2":
                newFix = "2D Fix";
                break;
            case "3":
                newFix = "3D Fix";
                break;
            }

            synchronized (data) {
                data.setSatellitesUsed(satsUsed);
                data.setHdop(newHdop);
                data.setFix(newFix);
            }
        }
    }

    // GSV — Satellite information
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-gsv.html
    private void parseGSV(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 4) {
            String totalSats = parts[3];
            if (!totalSats.isEmpty()) {
                int satCount = Integer.parseInt(totalSats);

                synchronized (data) {
                    if (parts[0].contains("BD")) {
                        data.setBeidouSatellites(satCount);
                    } else {
                        data.setVisibleSatellites(satCount);
                    }
                }
            }
        }
    }

    // RMC — Position, velocity, and time
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-rmc.html
    private void parseRMC(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 12) {
            String utc = parts[1];
            String status = parts[2];
            String lat = parts[3];
            String latDir = parts[4];
            String lon = parts[5];
            String lonDir = parts[6];
            String speed = parts[7]; // knots
            String course = parts[8];
            String date = parts[9];

            double newLat = 0, newLon = 0;
            float newSpeed = 0, newCourse = 0;
            boolean validCoords = false;
            boolean hasSpeed = false, hasCourse = false;

            if (status.equals("A")) { // A == active, V == void
                if (!lat.isEmpty() && !lon.isEmpty()) {
                    newLat = convertToDecimal(lat, latDir);
                    newLon = convertToDecimal(lon, lonDir);
                    validCoords = true;
                }

                if (!speed.isEmpty()) {
                    newSpeed = Float.parseFloat(speed);
                    hasSpeed = true;
                }
                if (!course.isEmpty()) {
                    newCourse = Float.parseFloat(course);
                    hasCourse = true;
                }
            }

            synchronized (data) {
                data.setFixTime(utc);
                data.setFixDate(date);
                if (validCoords) {
                    data.setLatitude(newLat);
                    data.setLongitude(newLon);
                    data.setValidFix(true);
                }
                if (hasSpeed) {
                    data.setSpeed(newSpeed);
                }
                if (hasCourse) {
                    data.setCourse(newCourse);
                }
            }
        }
    }

    // VTG — Track made good and speed over ground
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-vtg.html
    private void parseVTG(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 9) {
            String course = parts[1];
            String speed = parts[5];

            float newCourse = 0, newSpeed = 0;
            boolean hasCourse = false, hasSpeed = false;

            if (!course.isEmpty()) {
                newCourse = Float.parseFloat(course);
                hasCourse = true;
            }
            if (!speed.isEmpty()) {
                newSpeed = Float.parseFloat(speed);
                hasSpeed = true;
            }

            synchronized (data) {
                if (hasCourse) {
                    data.setCourse(newCourse);
                }
                if (hasSpeed) {
                    data.setSpeed(newSpeed);
                }
            }
        }
    }

    // ZDA — UTC day, month, and year, and local time zone offset
    // https://receiverhelp.trimble.com/alloy-gnss/en-us/nmea0183-messages-zda.html
    private void parseZDA(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 7) {
            String time = parts[1];
            String day = parts[2];
            String month = parts[3];
            String year = parts[4];

            if (!time.isEmpty() && !day.isEmpty() && !month.isEmpty() &&
                !year.isEmpty()) {
                String newUtcDateTime =
                    String.format("%s-%s-%s %s UTC", year, month, day, time);

                synchronized (data) {
                    data.setFixTime(time);
                    data.setUtcDateTime(newUtcDateTime);
                }
            }
        }
    }

    private void parseTXT(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 4) {
            String text = parts[4];
            if (text.contains("ANTENNA")) {
                synchronized (data) { data.setAntennaStatus(text); }
            }
        }
    }

    private double convertToDecimal(String dms, String dir) {
        if (dms.isEmpty())
            return 0.0;

        try {
            double raw = Double.parseDouble(dms);
            int degrees = (int)(raw / 100);
            double minutes = raw - (degrees * 100);
            double decimal = degrees + (minutes / 60.0);

            if ("S".equals(dir) || "W".equals(dir)) {
                decimal = -decimal;
            }

            return decimal;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Returns the live {@link GPSData} aggregator. Every parsed NMEA
     * sentence updates this object in place; readers see the most
     * recent values consistent with the per-field {@code volatile}
     * publication used inside {@code GPSData}.
     *
     * @return the live GPS data view
     */
    public GPSData getGPSData() { return this.data; }

    /**
     * Aggregated GPS state, updated by the background reader from the
     * stream of NMEA sentences. All fields are individually volatile so
     * external readers always see a published value, but consistency
     * <em>across</em> fields is only guaranteed inside a
     * {@code synchronized(data)} block.
     *
     * <p>Setters are package-private — only the parser writes to the
     * fields. The getters are intentionally simple accessors.
     */
    public class GPSData {
        private volatile double latitude = 0.0;
        private volatile double longitude = 0.0;
        private volatile boolean validFix = false;
        private volatile float speed = 0.0f;
        private volatile float course = 0.0f;
        private volatile float altitude = 0.0f;
        private volatile int satellitesUsed = 0;
        private volatile int visibleSatellites = 0;
        private volatile int beidouSatellites = 0;
        private volatile float hdop = 0.0f;
        private volatile String fixTime = "";
        private volatile String fixDate = "";
        private volatile String fixType = "No Fix";
        private volatile String antennaStatus = "";
        private volatile String utcDateTime = "";

        GPSData() {
            this.latitude = 0.0;
            this.longitude = 0.0;
            this.validFix = false;
            this.speed = 0.0f;
            this.course = 0.0f;
            this.altitude = 0.0f;
            this.satellitesUsed = 0;
            this.visibleSatellites = 0;
            this.beidouSatellites = 0;
            this.hdop = 0.0f;
            this.fixTime = "";
            this.fixDate = "";
            this.fixType = "No Fix";
            this.antennaStatus = "";
            this.utcDateTime = "";
        }

        private void setLatitude(double latitude) { this.latitude = latitude; }

        private void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        private void setValidFix(boolean validFix) { this.validFix = validFix; }

        private void setSpeed(float speed) { this.speed = speed; }

        private void setCourse(float course) { this.course = course; }

        private void setAltitude(float altitude) { this.altitude = altitude; }

        private void setSatellitesUsed(int satellitesUsed) {
            this.satellitesUsed = satellitesUsed;
        }

        private void setVisibleSatellites(int visibleSatellites) {
            this.visibleSatellites = visibleSatellites;
        }

        private void setBeidouSatellites(int beidouSatellites) {
            this.beidouSatellites = beidouSatellites;
        }

        private void setHdop(float hdop) { this.hdop = hdop; }

        private void setFixTime(String fixTime) { this.fixTime = fixTime; }

        private void setFixDate(String fixDate) { this.fixDate = fixDate; }

        private void setFix(String fixType) { this.fixType = fixType; }

        private void setAntennaStatus(String antennaStatus) {
            this.antennaStatus = antennaStatus;
        }

        private void setUtcDateTime(String utcDateTime) {
            this.utcDateTime = utcDateTime;
        }

        /** @return latitude in decimal degrees (positive = north) */
        public double getLatitude() { return latitude; }

        /** @return longitude in decimal degrees (positive = east) */
        public double getLongitude() { return longitude; }

        /** @return {@code true} when the receiver currently has a fix */
        public boolean hasValidFix() { return validFix; }

        /** @return ground speed in knots */
        public float getSpeed() { return speed; }

        /** @return course over ground in degrees true */
        public float getCourse() { return course; }

        /** @return altitude in metres above mean sea level */
        public float getAltitude() { return altitude; }

        /** @return number of satellites used in the current solution */
        public int getSatellitesUsed() { return satellitesUsed; }

        /** @return total satellites visible (across all constellations) */
        public int getVisibleSatellites() { return visibleSatellites; }

        /** @return BeiDou satellites visible (subset reported separately by some receivers) */
        public int getBeidouSatellites() { return beidouSatellites; }

        /** @return horizontal dilution of precision */
        public float getHDOP() { return hdop; }

        /** @return UTC time of the last fix as raw NMEA hhmmss[.sss] */
        public String getFixTime() { return fixTime; }

        /** @return UTC date of the last fix as raw NMEA ddmmyy */
        public String getFixDate() { return fixDate; }

        /** @return human-readable fix-type label (e.g., "GPS Fix", "DGPS Fix", "No Fix") */
        public String getFixType() { return fixType; }

        /** @return antenna status text from the receiver (when available) */
        public String getAntennaStatus() { return antennaStatus; }

        /** @return UTC date and time formatted as {@code yyyy-MM-dd HH:mm:ss UTC} from the ZDA sentence */
        public String getUtcDateTime() { return utcDateTime; }

        public String toString() {
            return String.format(
                "Fix: %s | Lat: %.6f | Lon: %.6f | Speed: %.1f knots | Course: "
                    + "%.1f° | Altitude: %.1fm | Satellites: %d/%d | BeiDou: %d"
                    + " | HDOP: %.1f | Time: %s | Antenna Status: %s",
                fixType, latitude, longitude, speed, course, altitude,
                satellitesUsed, visibleSatellites, beidouSatellites, hdop,
                utcDateTime.isEmpty() ? fixTime : utcDateTime, antennaStatus);
        }
    }
}
