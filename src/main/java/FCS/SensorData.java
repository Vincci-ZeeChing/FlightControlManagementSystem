package FCS;


import com.rabbitmq.client.Channel;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.TimerTask;

class SensorDataUpdater extends TimerTask {
    private final Channel channel;

    public SensorDataUpdater(Channel channel) {
        this.channel = channel;
    }

    public void run() {
        // get updated sensor data every 3 seconds
        String altitude = getAltitude();
        String cabinPressure = getCabinPressure();
        String speed = getSpeed();
        String weatherConditions = getWeatherConditions();
        int flightNumber = 0;


        // send the updated data to the decision system through the RabbitMQ queue
        try {
            // sent sensor data
            flightNumber = new Random().nextInt(1000, 9999);
            String randomString = "";
            Random random = new Random();
            for (int i = 0; i < 3; i++) {
                char character = (char) (random.nextInt(26) + 'A'); // generate a random uppercase letter
                randomString += character;
            }
            // Flight number
            System.out.println("\n------------------------");
            System.out.println("\tFlight: " + randomString + " " + flightNumber);
            System.out.println("------------------------");
            // Ready Take off
            System.out.println("MESSAGE: Engine start...");
            System.out.println("MESSAGE: Ready for Take Off!");
            String engineMessage = "COMMAND: Increasing Engine Power...";
            channel.basicPublish("", FlightControlSystem.ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
            System.out.println(engineMessage);
            String wingFlaps = "COMMAND: Reducing drag and increasing lift of Wing Flaps...";
            channel.basicPublish("", FlightControlSystem.WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
            System.out.println(wingFlaps);
            String tailFlaps = "COMMAND: Adjusting Tail Flaps to pitch attitude... ";
            channel.basicPublish("", FlightControlSystem.TAIL_FLAPS_QUEUE_NAME, null, tailFlaps.getBytes());
            System.out.println(tailFlaps);
            // Successful Take Off
            System.out.println("MESSAGE: Successful Take Off!");
            String landingGear = "COMMAND: Retracting Landing Gear...";
            channel.basicPublish("", FlightControlSystem.LANDING_GEAR_QUEUE_NAME, null, landingGear.getBytes());
            System.out.println(landingGear);
            // Display Sensor Data
            System.out.println("MESSAGE: Display Sensor data...\n");
            channel.basicPublish("", FlightControlSystem.ALTITUDE_SENSOR_QUEUE, null, altitude.getBytes());
            System.out.println(altitude);
            channel.basicPublish("", FlightControlSystem.CABIN_SENSOR_QUEUE, null, cabinPressure.getBytes());
            System.out.println(cabinPressure);
            channel.basicPublish("", FlightControlSystem.SPEED_SENSOR_QUEUE, null, speed.getBytes());
            System.out.println(speed);
            channel.basicPublish("", FlightControlSystem.WEATHER_SENSOR_QUEUE, null, weatherConditions.getBytes());
            System.out.println(weatherConditions);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAltitude() {
        // simulate data from altitude sensor
        int altitude = new Random().nextInt(18000, 45000); // generate a random number between 18000 and 45000
        String altitudeStr = String.valueOf(altitude);
        DecimalFormat df = new DecimalFormat("#.##");
        double longitude = Double.parseDouble(df.format(Math.random() * 360 - 180));
        String longitudeStr = String.valueOf(longitude);
        double latitude = Double.parseDouble(df.format(Math.random() * 180 - 90));
        String latitudeStr = String.valueOf(latitude);

        // return formatted sensor data as string
        return "\tAltitude Sensor" +
                "\nAltitude: " + altitudeStr + " ft" +
                "\nLongitude: " + longitudeStr + "°" +
                "\nLatitude: " + latitudeStr + "°" +
                "\n------------------------";
    }

    public String getCabinPressure() {
        // simulate data from cabin sensor
        double temperature = getCabinTemperature();
        String temperatureStr = String.format("%.2f", temperature);
        double pressure = getCabinPressureValue();
        String pressureStr = String.format("%.2f", pressure);
        int oxygen = getOxygenLevel(pressure);
        String oxygenStr = String.valueOf(oxygen);

        // return formatted sensor data as string
        return "\tCabin Sensor" +
                "\nTemperature: " + temperatureStr + " °C" +
                "\nPressure: " + pressureStr + " psi" +
                "\nOxygen: " + oxygenStr + " %" +
                "\n------------------------";
    }

    private double getCabinTemperature() {
        // code to get cabin temperature from sensors
        return new Random().nextDouble(16, 26);
    }

    public double getCabinPressureValue() {
        // code to get  cabin pressure from sensors
        return new Random().nextDouble(1, 13);
    }

    private int getOxygenLevel(double pressure) {
        // code to get cabin oxygen level from sensors
        int oxygen = 0;
        if (pressure <= 3) {
            oxygen = (int) (new Random().nextDouble(1, 20));
        } else if (pressure > 3 && pressure <= 10) {
            oxygen = (int) (new Random().nextDouble(21, 70));
        } else if (pressure > 10 && pressure <= 13) {
            oxygen = (int) (new Random().nextDouble(71, 99));
        }
        return oxygen;
    }

    private String getSpeed() {
        // simulate data from speed sensor
        // get altitude data
        String altitudeData = getAltitude();
        String[] altitudeParts = altitudeData.split("\n");
        String altitudeStr = altitudeParts[1].substring(10, altitudeParts[1].length() - 3);
        int altitude = Integer.parseInt(altitudeStr);

        // calculate speed based on altitude
        int speed = 0;
        if (altitude > 40000) {
            // altitude more than 40000, then speed between 800 and 960
            speed = (int) (new Random().nextDouble(800, 960));
        } else if (altitude < 30000) {
            // altitude less than 30000, then speed between 650 and 800
            speed = (int) (new Random().nextDouble(650, 800));
        } else {
            // general altitude, then speed between 700 and 900
            speed = (int) (new Random().nextDouble(700, 900));
        }
        String speedStr = String.valueOf(speed);

        // return formatted sensor data as string
        return "\tSpeed Sensor" +
                "\nSpeed: " + speedStr + " Km/h" +
                "\n------------------------";
    }

    private String getWeatherConditions() {
        // Get altitude data
        String altitudeData = getAltitude();
        String[] altitudeParts = altitudeData.split("\n");
        String altitudeStr = altitudeParts[1].substring(10, altitudeParts[1].length() - 3);
        int altitude = Integer.parseInt(altitudeStr);

        // Simulate data from weather sensor
        String condition = null;
        String[] extremelyGoodConditions = {"Sunny"};
        String[] fairConditions = {"Rainy", "Cloudy", "Snowy"};
        String[] poorConditions = {"Stormy", "Heavy Foggy", "Heavy Snowy"};
        double temperature = getTemperature();
        String temperatureStr = String.format("%.2f", temperature);
        double humidity = getHumidity();
        String humidityStr = String.format("%.2f", humidity);
        double windSpeed = getWindSpeed();
        String windSpeedStr = String.format("%.2f", windSpeed);
        double windDirection = getWindDirection();
        String windDirectionStr = String.format("%.2f", windDirection);

        if (altitude >= 40000) {
            // altitude more than 40000, which is good condition
            int index = (int) (Math.random() * extremelyGoodConditions.length);
            condition = extremelyGoodConditions[index];
        } else if (altitude >= 30000) {
            // altitude more than 30000, which is fair condition
            int index = (int) (Math.random() * fairConditions.length);
            condition = fairConditions[index];
        } else {
            // altitude less than 30000, which is poor condition
            int index = (int) (Math.random() * poorConditions.length);
            condition = poorConditions[index];
        }
        double visibility = getVisibility(condition);
        String visibilityStr = String.format("%.2f", visibility);

        // return formatted sensor data as string
        return "\tWeather Sensor" +
                "\nCondition: " + condition +
                "\nTemperature: " + temperatureStr + " °C" +
                "\nHumidity: " + humidityStr + " %" +
                "\nWind Speed: " + windSpeedStr + " mph" +
                "\nWind Direction: " + windDirectionStr + " °" +
                "\nVisibility: " + visibilityStr + " mi" +
                "\n------------------------\n";
    }

    private double getTemperature() {
        // generate a random temperature between -30°C and 40°C
        return new Random().nextDouble(-30, 40);
    }

    private double getHumidity() {
        // generate a random humidity between 0% and 100%
        return new Random().nextDouble(0, 100);
    }

    private double getWindSpeed() {
        // generate a random wind speed between 0 mph and 100 mph
        return new Random().nextDouble(0, 100);
    }

    private double getWindDirection() {
        // generate a random wind direction between 0° and 360°
        return new Random().nextDouble(0, 360);
    }

    private double getVisibility(String condition) {
        //visibility base on condition
        return switch (condition) {
            case "Heavy Foggy", "Heavy Snowy" -> new Random().nextDouble(0, 2);
            case "Stormy", "Rainy", "Cloudy", "Snowy" -> new Random().nextDouble(3, 6);
            case "Sunny" -> new Random().nextDouble(7, 10);
            default -> 0;
        };
    }
}
