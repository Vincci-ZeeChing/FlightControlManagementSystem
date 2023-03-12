package FCS;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
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


        String ALTITUDE_SENSOR_QUEUE = "Altitude_Sensor";
        String SPEED_SENSOR_QUEUE = "Speed_Sensor";
        String CABIN_SENSOR_QUEUE = "Cabin_Sensor";
        String WEATHER_SENSOR_QUEUE = "Weather_Sensor";


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
        timer.schedule(new SensorDataUpdater(channel), 0, 7000);


        // decision-making
        Consumer altitudeConsumer = new DefaultConsumer(altitudeChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String SensorData = new String(body, StandardCharsets.UTF_8);
//
                // Parse the message and extract the altitude value
                if (SensorData.contains("Altitude Sensor")) {
                    String altitudeStr = SensorData.split("\n")[1].split(" ")[1];
                    int altitude = Integer.parseInt(altitudeStr);
                    if (altitude > 40000) {
                        // Activate the descent actuator
                        System.out.println("WARNING: Altitude is too high. Reduce altitude immediately!");
                        String engineMessage = "COMMAND: Decreasing Engine Power...";
                        channel.basicPublish("", ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);

                        String wingFlaps = "COMMAND: Increasing drag and reducing lift of Wing Flaps...";
                        channel.basicPublish("", WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);


                    } else if (altitude < 20000) {
                        System.out.println("WARNING: Altitude is too low. Increase altitude immediately!");
                        String engineMessage = "COMMAND: Increasing Engine Power...";
                        channel.basicPublish("", ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);

                        String wingFlaps = "COMMAND: Reducing drag and increasing lift of Wing Flaps...";
                        channel.basicPublish("", WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);

                        String tailFlaps = "COMMAND: Adjusting Tail Flaps to pitch attitude... ";
                        channel.basicPublish("", TAIL_FLAPS_QUEUE_NAME, null, tailFlaps.getBytes());
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
                String SensorData = new String(body, StandardCharsets.UTF_8);
                if (SensorData.contains("Cabin Sensor")) {
                    String temperatureStr = SensorData.split("\n")[1].split(" ")[1];
                    double temperature = Double.parseDouble(temperatureStr);
                    if (temperature > 23) {
                        System.out.println("WARNING: Cabin Temperature is too high. Turn on air conditioning!");
                        // send actuator data to turn on the air conditioning
                        String temperatureActuator = "COMMAND: Turn on Air Conditioning...";
                        channel.basicPublish("", CABIN_TEMPERATURE_QUEUE_NAME, null, temperatureActuator.getBytes());
                        System.out.println(temperatureActuator);
                    } else if (temperature < 20) {
                        System.out.println("WARNING: Cabin Temperature is too low. Turn off air conditioning!");
                        // send actuator data to turn off the air conditioning
                        String temperatureActuator = "COMMAND: Turn off Air Conditioning...";
                        channel.basicPublish("", CABIN_TEMPERATURE_QUEUE_NAME, null, temperatureActuator.getBytes());
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
                        channel.basicPublish("", OXYGEN_MASKS_QUEUE_NAME, null, oxygenMask.getBytes());
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
                String SensorData = new String(body, StandardCharsets.UTF_8);
                if (SensorData.contains("Rainy")){
                    System.out.println("WARNING: Raining now!");
                    String wiper = "COMMAND: Turn on the wipers...";
                    channel.basicPublish("", LANDING_GEAR_QUEUE_NAME, null, wiper.getBytes());
                    System.out.println(wiper);
                }
                if (SensorData.contains("Weather Sensor")) {
                    String visibilityStr = SensorData.split("\n")[6].split(" ")[1];
                    double visibility = Double.parseDouble(visibilityStr);
                    if (visibility <= 1) {
                        System.out.println("WARNING: Visibility is very low.");
                        System.out.println("WARNING: Landing is REQUIRE! Reduce altitude!");
                        String engineMessage = "COMMAND: Decreasing Engine Power...";
                        channel.basicPublish("", ENGINE_QUEUE_NAME, null, engineMessage.getBytes());
                        System.out.println(engineMessage);
                        String wingFlaps = "COMMAND: Increasing drag and reducing lift of Wing Flaps...";
                        channel.basicPublish("", WING_FLAPS_QUEUE_NAME, null, wingFlaps.getBytes());
                        System.out.println(wingFlaps);
                        System.out.println("MESSAGE: 1000 ft above the ground.");
                        String landingGear = "COMMAND: Deploy Landing Gear...";
                        channel.basicPublish("", LANDING_GEAR_QUEUE_NAME, null, landingGear.getBytes());
                        System.out.println(landingGear);
                        System.out.println("MESSAGE: Prepare Landing...");
                        System.out.println("MESSAGE: Safe Landing and Arrival!");
                    } else if (visibility < 5) {
                        System.out.println("MESSAGE: Visibility was a little low, but it was still possible to continue flying.");
                    } else {
                        System.out.println("MESSAGE: Visibility is very clear.");

                    }
                }
            }
        };

        Thread altitudeThread = new Thread(() -> {
            try {
                channel.basicConsume(ALTITUDE_SENSOR_QUEUE, true, altitudeConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread cabinThread = new Thread(() -> {
            try {
                channel.basicConsume(CABIN_SENSOR_QUEUE, true, cabinConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread weatherThread = new Thread(() -> {
            try {
                channel.basicConsume(WEATHER_SENSOR_QUEUE, true, weatherConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        altitudeThread.start();
        cabinThread.start();
        weatherThread.start();

    }
}




