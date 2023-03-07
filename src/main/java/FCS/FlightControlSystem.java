package FCS;

import com.rabbitmq.client.*;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class FlightControlSystem {
    public final static String ALTITUDE_SENSOR_QUEUE = "Altitude_Sensor";
    public final static String SPEED_SENSOR_QUEUE = "Speed_Sensor";
    public final static String CABIN_SENSOR_QUEUE = "Cabin_Sensor";
    public final static String WEATHER_SENSOR_QUEUE = "Weather_Sensor";
    private final static String ENGINE_QUEUE_NAME = "Engine_Actuator";
    private final static String WING_FLAPS_QUEUE_NAME = "WingFlaps_Actuator";
    private final static String TAIL_FLAPS_QUEUE_NAME = "TailFlaps_Actuator";
    private final static String LANDING_GEAR_QUEUE_NAME = "LandingGear_Actuator";
    private final static String OXYGEN_MASKS_QUEUE_NAME = "Oxygen_Masks_Actuator";


    public static void main(String[] args) throws IOException, TimeoutException {
        // create RabbitMQ connection and channel
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // set up and declare channel
        // sensory system
        channel.queueDeclare(ALTITUDE_SENSOR_QUEUE,false,false,false,null);
        channel.queueDeclare(SPEED_SENSOR_QUEUE,false,false,false,null);
        channel.queueDeclare(CABIN_SENSOR_QUEUE,false,false,false,null);
        channel.queueDeclare(WEATHER_SENSOR_QUEUE,false,false,false,null);
        channel.queueDeclare(ENGINE_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(WING_FLAPS_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(TAIL_FLAPS_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(LANDING_GEAR_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(OXYGEN_MASKS_QUEUE_NAME,false,false,false,null);

        // actuator system
//        channel.queueDeclare(ACTUATOR_QUEUE_NAME,false,false,false,null);

        // heading
        System.out.println("--------------------------------");
        System.out.println("     Flight Control System");
        System.out.println("--------------------------------\n");


        // update data every 5 second
        Timer timer = new Timer();
        timer.schedule(new SensorDataUpdater(channel), 0, 5000);


//         create consumer to receive updated sensor data from the altitude sensor queue
        Consumer altitudeConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String altitudeData = new String(body, "UTF-8");
                // make decision based on the altitude data
                if (altitudeData.contains("ft")) {
                    String[] altitudeArr = altitudeData.split(": ");
                    int altitude = Integer.parseInt(altitudeArr[1].split(" ")[0]);
                    if (altitude > 42000) {
                        System.out.println("WARNING: Altitude is too high. Reduce altitude immediately!");
                        // send actuator data to adjust the altitude of the plane
                        String enginePower = "Reduce Engine Power and Altitude";
                        channel.basicPublish("", FlightControlSystem.ENGINE_QUEUE_NAME, null, enginePower.getBytes());
//                        String wingFlaps = "Increase Wing Flaps lift to maintain speed and avoid stalling";
//                        channel.basicPublish("", FlightControlSystem.WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
//                        String tailFlaps = "Tail flap adjusted to maintain a desired pitch attitude";
//                        channel.basicPublish("", FlightControlSystem.TAIL_FLAPS_QUEUE_NAME, null, tailFlaps.getBytes());
                    } else {
                        System.out.println("Altitude is safe. No action required.");
                    }
                }
            }
        };
        channel.basicConsume(FlightControlSystem.ENGINE_QUEUE_NAME, true, altitudeConsumer);
//        channel.basicConsume(FlightControlSystem.WING_FLAPS_QUEUE_NAME, true, altitudeConsumer);
//        channel.basicConsume(FlightControlSystem.TAIL_FLAPS_QUEUE_NAME, true, altitudeConsumer);
//        channel.basicConsume(FlightControlSystem.ALTITUDE_SENSOR_QUEUE, true, altitudeConsumer);




//        // create consumer to receive updated sensor data from the cabin sensor queue
//        Consumer cabinConsumer = new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                String cabinData = new String(body, "UTF-8");
//                // make decision based on the cabin data
//                if (cabinData.contains("Temperature")) {
//                    String[] tempArr = cabinData.split(": ");
//                    double temperature = Double.parseDouble(tempArr[1].split(" ")[0]);
//                    if (temperature > 26) {
//                        System.out.println("WARNING: Temperature is too high. Turn on air conditioning!");
//                        // send actuator data to turn on the air conditioning
//                        String actuatorData = "Turn on Air Conditioning";
//                        channel.basicPublish("", FlightControlSystem.ACTUATOR_QUEUE_NAME, null, actuatorData.getBytes());
//                    } else if (temperature < 20) {
//                        System.out.println("WARNING: Temperature is too low. Turn on air conditioning!");
//                        // send actuator data to turn on the air conditioning
//                        String actuatorData = "Turn off Air Conditioning";
//                        channel.basicPublish("", FlightControlSystem.ACTUATOR_QUEUE_NAME, null, actuatorData.getBytes());
//                    }else {
//                        System.out.println("Temperature is Normal. No action required");
//                    }
//                }
//            }
//        };
//        // create consumer to receive updated sensor data from the weather sensor queue
//        Consumer weatherConsumer = new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                String weatherData = new String(body, "UTF-8");
//                // make decision based on the weather data
//                if (weatherData.contains("Stormy")) {
//                    System.out.println("WARNING: Weather conditions are dangerous. Reduce altitude immediately!");
//                    // send actuator data to adjust the altitude of the plane
//                    String actuatorData = "Reduce Altitude";
//                    channel.basicPublish("", FlightControlSystem.ACTUATOR_QUEUE_NAME, null, actuatorData.getBytes());
//                } else if (weatherData.contains("Heavy Foggy")) {
//                    System.out.println("WARNING: Visibility is reduced due to heavy fog. Turn on landing lights!");
//                    // send actuator data to turn on the landing lights
//                    String actuatorData = "Turn on Landing Lights and prepare for landing.";
//                    channel.basicPublish("", FlightControlSystem.ACTUATOR_QUEUE_NAME, null, actuatorData.getBytes());
//                } else {
//                    System.out.println("Weather conditions are safe. No action required.");
//                }
//            }
//        };
//
//        channel.basicConsume(FlightControlSystem.ALTITUDE_SENSOR_QUEUE, true, altitudeConsumer);
//        channel.basicConsume(FlightControlSystem.CABIN_SENSOR_QUEUE, true, cabinConsumer);
//        channel.basicConsume(FlightControlSystem.WEATHER_SENSOR_QUEUE, true, weatherConsumer);
//
//
//
    }
}


class SensorDataUpdater extends TimerTask{
    private Channel channel;

    public SensorDataUpdater(Channel channel){
        this.channel = channel;
    }

    public void run() {
        // get updated sensor data every 3 seconds
        String altitude = getAltitude();
        String cabinPressure = getCabinPressure();
        String speed = getSpeed();
        String weatherConditions = getWeatherConditions();

        // send the updated data to the decision system through the RabbitMQ queue
        try {
            String altitudeData = altitude;
            String cabinPressureData = cabinPressure;
            String speedData = speed;
            String weatherData = weatherConditions;
            // sent sensor data
            channel.basicPublish("",FlightControlSystem.ALTITUDE_SENSOR_QUEUE, null, altitudeData.getBytes());
            System.out.println(altitudeData);
            channel.basicPublish("",FlightControlSystem.CABIN_SENSOR_QUEUE, null, cabinPressureData.getBytes());
            System.out.println(cabinPressureData);
            channel.basicPublish("",FlightControlSystem.SPEED_SENSOR_QUEUE, null, speedData.getBytes());
            System.out.println(speedData);
            channel.basicPublish("",FlightControlSystem.WEATHER_SENSOR_QUEUE, null, weatherData.getBytes());
            System.out.println(weatherData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAltitude() {
        // simulate data from altitude sensor
        int altitude = new Random().nextInt(20000,45000); // generate a random number between 20000 and 45000
        String altitudeStr = String.valueOf(altitude);
        DecimalFormat df = new DecimalFormat("#.##");
        double longitude = Double.parseDouble(df.format(Math.random() * 360 - 180));
        String longitudeStr = String.valueOf(longitude);
        double latitude = Double.parseDouble(df.format(Math.random() * 180 - 90));
        String latitudeStr = String.valueOf(latitude);
        return "\tAltitude Sensor"+
                "\nAltitude: " + altitudeStr + " ft" +
                "\nLongitude: " +  longitudeStr + "°" +
                "\nLatitude: " + latitudeStr + "°" +
                "\n----------------------";
    }

    private String getCabinPressure() {
        // get actual data from cabin sensors
        double temperature = getCabinTemperature();
        String temperatureStr = String.format("%.2f", temperature);
        double pressure = getCabinPressureValue();
        String pressureStr = String.format("%.2f", pressure);
        int oxygen = getOxygenLevel();
        String oxygenStr = String.valueOf(oxygen);

        // return formatted sensor data as string
        return "\tCabin Sensor"+
                "\nTemperature: " + temperatureStr + " °C" +
                "\nPressure: " + pressureStr + " psi" +
                "\nOxygen: " + oxygenStr + " %" +
                "\n----------------------";
    }

    // example methods to get actual sensor data
    private double getCabinTemperature() {
        // code to get cabin temperature from sensors
        double temperature = new Random().nextDouble(20,26);
        return temperature;
    }

    public double getCabinPressureValue() {
        // code to get  cabin pressure from sensors
        double pressure = new Random().nextDouble(1,13);
        return pressure;
    }

    private int getOxygenLevel() {
        // code to get cabin oxygen level from sensors
        double pressure = getCabinPressureValue();
        int oxygen = 0;
        if (pressure <= 2.7) {
            oxygen = (int) (new Random().nextDouble(1,20));
        } else if (pressure > 2.7 && pressure <= 10) {
            oxygen = (int) (new Random().nextDouble(21,70));
        } else if (pressure > 10) {
            oxygen = (int) (new Random().nextDouble(71,99));
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
            speed = (int) (new Random().nextDouble(800,960));
        } else if (altitude < 30000) {
            speed = (int) (new Random().nextDouble(650,800));
        }else {
            speed = (int) (new Random().nextDouble(700,900));
        }
        String speedStr = String.valueOf(speed);

        // return formatted sensor data as string
        return "\tSpeed Sensor"+
                "\nSpeed: " + speedStr + " Km/h" +
                "\n----------------------";
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
        if (altitude >= 40000) {
            int index = (int) (Math.random() * extremelyGoodConditions.length);
            condition = extremelyGoodConditions[index];
        } else if (altitude >= 30000) {
            int index = (int) (Math.random() * fairConditions.length);
            condition = fairConditions[index];
        } else {
            int index = (int) (Math.random() * poorConditions.length);
            condition = poorConditions[index];
        }

        if (condition == null) {
            return "\tWeather Sensor" +
                    "\nCondition: Unknown" +
                    "\n----------------------\n";
        } else {
            return "\tWeather Sensor" +
                    "\nCondition: " + condition +
                    "\n----------------------\n";
        }
    }
}

