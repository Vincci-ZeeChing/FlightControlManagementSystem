package FCS;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class FlightControlSystem {
    public final static String ALTITUDE_SENSOR_QUEUE = "Altitude_Sensor";
    public final static String SPEED_SENSOR_QUEUE = "Speed_Sensor";
    public final static String CABIN_SENSOR_QUEUE = "Cabin_Sensor";
    public final static String WEATHER_SENSOR_QUEUE = "Weather_Sensor";
    public final static String ENGINE_QUEUE_NAME = "Engine_Actuator";
    public final static String WING_FLAPS_QUEUE_NAME = "WingFlaps_Actuator";
    public final static String TAIL_FLAPS_QUEUE_NAME = "TailFlaps_Actuator";
    public final static String LANDING_GEAR_QUEUE_NAME = "LandingGear_Actuator";
    public final static String OXYGEN_MASKS_QUEUE_NAME = "Oxygen_Masks_Actuator";
    public final static String CABIN_TEMPERATURE_QUEUE_NAME = "Cabin_Temperature_Actuator";
    public final static String WIPER_QUEUE_NAME = "Wiper_Actuator";


    public static void main(String[] args) throws IOException, TimeoutException {
        // create RabbitMQ connection and channel
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        Channel altitudeChannel = connection.createChannel();
        Channel cabinChannel = connection.createChannel();
        Channel weatherChannel = connection.createChannel();

        // set up and declare channel
        // sensory system
        altitudeChannel.queueDeclare(ALTITUDE_SENSOR_QUEUE, false, false, false, null);
        channel.queueDeclare(SPEED_SENSOR_QUEUE, false, false, false, null);
        cabinChannel.queueDeclare(CABIN_SENSOR_QUEUE, false, false, false, null);
        weatherChannel.queueDeclare(WEATHER_SENSOR_QUEUE, false, false, false, null);
        // actuator system
        channel.queueDeclare(ENGINE_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(WING_FLAPS_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(TAIL_FLAPS_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(LANDING_GEAR_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(OXYGEN_MASKS_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(CABIN_TEMPERATURE_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(WIPER_QUEUE_NAME, false, false, false, null);


        // heading
        System.out.println("\n========================");
        System.out.println(" Flight Control System");
        System.out.println("========================");


        // update data every 5 second
        Timer timer = new Timer();
        timer.schedule(new SensorDataUpdater(channel), 0, 5000);

        Consumer altitudeConsumer = new DefaultConsumer(altitudeChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String SensorData = new String(body, "UTF-8");
//
                // Parse the message and extract the altitude value
                if (SensorData.contains("Altitude Sensor")) {
                    String altitudeStr = SensorData.split("\n")[1].split(" ")[1];
                    int altitude = Integer.parseInt(altitudeStr);
                    if (altitude > 40000) {
                        // Activate the descent actuator
                        System.out.println("WARNING: Altitude is too high. Reduce altitude immediately!");
                        String engineMessage = "COMMAND: Decreasing Engine Power...";
                        channel.basicPublish("", FlightControlSystem.ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);

                        String wingFlaps = "COMMAND: Increasing drag and reducing lift of Wing Flaps...";
                        channel.basicPublish("", FlightControlSystem.WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);


                    } else if (altitude < 20000) {
                        System.out.println("WARNING: Altitude is too low. Increase altitude immediately!");
                        String engineMessage = "COMMAND: Increasing Engine Power...";
                        channel.basicPublish("", FlightControlSystem.ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);

                        String wingFlaps = "COMMAND: Reducing drag and increasing lift of Wing Flaps...";
                        channel.basicPublish("", FlightControlSystem.WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);

                        String tailFlaps = "COMMAND: Adjusting Tail Flaps to pitch attitude... ";
                        channel.basicPublish("", FlightControlSystem.TAIL_FLAPS_QUEUE_NAME, null, tailFlaps.getBytes());
                        System.out.println(tailFlaps);
                    } else {
                        System.out.println("MESSAGE: Altitude is safe. No action required.");
                    }
                }
            }
        };


        Consumer cabinConsumer = new DefaultConsumer(cabinChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String SensorData = new String(body, "UTF-8");
                if (SensorData.contains("Cabin Sensor")) {
                    String temperatureStr = SensorData.split("\n")[1].split(" ")[1];
                    double temperature = Double.parseDouble(temperatureStr);
                    if (temperature > 23) {
                        System.out.println("WARNING: Cabin Temperature is too high. Turn on air conditioning!");
                        // send actuator data to turn on the air conditioning
                        String temperatureActuator = "COMMAND: Turn on Air Conditioning...";
                        channel.basicPublish("", FlightControlSystem.CABIN_TEMPERATURE_QUEUE_NAME, null, temperatureActuator.getBytes());
                        System.out.println(temperatureActuator);
                    } else if (temperature < 20) {
                        System.out.println("WARNING: Cabin Temperature is too low. Turn off air conditioning!");
                        // send actuator data to turn off the air conditioning
                        String temperatureActuator = "COMMAND: Turn off Air Conditioning...";
                        channel.basicPublish("", FlightControlSystem.CABIN_TEMPERATURE_QUEUE_NAME, null, temperatureActuator.getBytes());
                        System.out.println(temperatureActuator);
                    } else {
                        System.out.println("MESSAGE: Normal Temperature. No action required.");
                    }
                }
                if (SensorData.contains("Cabin Sensor")) {
                    // get pressure data
                    String pressureStr = SensorData.split("\n")[2].split(" ")[1];
                    double pressure = Double.parseDouble(pressureStr);
                    if (pressure <= 3) {
                        System.out.println("WARNING: Sudden Loss Cabin Pressure and Oxygen Level is too low. Oxygen Mask is REQUIRE!");
                        String oxygenMask = "COMMAND: Drop down oxygen masks to all passengers...";
                        channel.basicPublish("", FlightControlSystem.OXYGEN_MASKS_QUEUE_NAME, null, oxygenMask.getBytes());
                        System.out.println(oxygenMask);
                    } else {
                        System.out.println("MESSAGE: Pressure and Oxygen at normal level. No action required.");
                    }
                }
            }
        };

        Consumer weatherConsumer = new DefaultConsumer(weatherChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String SensorData = new String(body, "UTF-8");
                if (SensorData.contains("Weather Sensor")) {
                    String visibilityStr = SensorData.split("\n")[6].split(" ")[1];
                    double visibility = Double.parseDouble(visibilityStr);
                    if(visibility < 1) {
                        System.out.println("WARNING: Visibility is very low.");
                        System.out.println("WARNING: Landing is REQUIRE! Reduce altitude!");
                        String engineMessage = "COMMAND: Decreasing Engine Power...";
                        channel.basicPublish("", FlightControlSystem.ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);
                        String wingFlaps = "COMMAND: Increasing drag and reducing lift of Wing Flaps...";
                        channel.basicPublish("", FlightControlSystem.WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);
                        System.out.println("MESSAGE: 1000 ft above the ground.");
                        String landingGear = "COMMAND: Deploy Landing Gear...";
                        channel.basicPublish("", FlightControlSystem.LANDING_GEAR_QUEUE_NAME, null, landingGear.getBytes());
                        System.out.println(landingGear);
                        System.out.println("MESSAGE: Prepare Landing...");
                        System.out.println("MESSAGE: Safe Landing and Arrival!");
                    }else{
                        System.out.println("MESSAGE: Visibility is clear.");

                    }
                }
            }
        };

        try {
            altitudeChannel.basicConsume(FlightControlSystem.ALTITUDE_SENSOR_QUEUE, true, altitudeConsumer);
            cabinChannel.basicConsume(FlightControlSystem.CABIN_SENSOR_QUEUE, true, cabinConsumer);
            weatherChannel.basicConsume(FlightControlSystem.WEATHER_SENSOR_QUEUE, true, weatherConsumer);

        } catch (Exception e) {


        }


    }

}


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
            String altitudeData = altitude;
            String cabinPressureData = cabinPressure;
            String speedData = speed;
            String weatherData = weatherConditions;
            // sent sensor data
            flightNumber = new Random().nextInt(1000, 9999);
            String randomString = "";
            Random random = new Random();
            for (int i = 0; i < 3; i++) {
                char character = (char)(random.nextInt(26) + 'A'); // generate a random uppercase letter
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
            channel.basicPublish("", FlightControlSystem.ALTITUDE_SENSOR_QUEUE, null, altitudeData.getBytes());
            System.out.println(altitudeData);
            channel.basicPublish("", FlightControlSystem.CABIN_SENSOR_QUEUE, null, cabinPressureData.getBytes());
            System.out.println(cabinPressureData);
            channel.basicPublish("", FlightControlSystem.SPEED_SENSOR_QUEUE, null, speedData.getBytes());
            System.out.println(speedData);
            channel.basicPublish("", FlightControlSystem.WEATHER_SENSOR_QUEUE, null, weatherData.getBytes());
            System.out.println(weatherData);

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
        double temperature = new Random().nextDouble(16, 26);
        return temperature;
    }

    public double getCabinPressureValue() {
        // code to get  cabin pressure from sensors
        double pressure = new Random().nextDouble(1, 13);
        return pressure;
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
        String[] fairConditions = {"Light Rainy", "Cloudy", "Snowy"};
        String[] poorConditions = {"Stormy", "Heavy Foggy", "Heavy Rainy"};
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
        if (condition == null) {
            return "\tWeather Sensor" +
                    "\nCondition: Unknown" +
                    "\n------------------------\n";
        } else {
            return "\tWeather Sensor" +
                    "\nCondition: " + condition +
                    "\nTemperature: " + temperatureStr + " °C" +
                    "\nHumidity: " + humidityStr + " %" +
                    "\nWind Speed: " + windSpeedStr + " mph" +
                    "\nWind Direction: " + windDirectionStr + " °" +
                    "\nVisibility: " + visibilityStr + " mi" +
                    "\n------------------------\n";
        }
    }

    private double getTemperature() {
        // generate a random temperature between -30°C and 40°C
        double temperature = new Random().nextDouble(-30, 40);
        return temperature;
    }

    private double getHumidity() {
        // generate a random humidity between 0% and 100%
        double humidity = new Random().nextDouble(0, 100);
        return humidity;
    }

    private double getWindSpeed() {
        // generate a random wind speed between 0 mph and 100 mph
        double windSpeed = new Random().nextDouble(0, 100);
        return windSpeed;
    }

    private double getWindDirection() {
        // generate a random wind direction between 0° and 360°
        double windDirection = new Random().nextDouble(0, 360);
        return windDirection;
    }

    private double getVisibility(String condition) {
        //visibility base on condition
        double visibility = 0;

        if ( condition.equals("Heavy Foggy") || condition.equals("Heavy Rainy") || condition.equals("Snowy")) {
            visibility = new Random().nextDouble(0, 1);
        } else if (condition.equals("Stormy") || condition.equals("Light Rainy") || condition.equals("Cloudy")) {
            visibility = new Random().nextDouble(3, 7);
        } else if (condition.equals("Sunny")) {
            visibility = new Random().nextDouble(0, 1);

        }
        return visibility;
    }
}



