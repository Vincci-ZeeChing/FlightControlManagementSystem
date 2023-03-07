package FCS;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;


public class FlightControlSystemXX {

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        // create RabbitMQ connection and channel
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        System.out.println("Waiting for messages...");



        // close the channel and connection
        Scanner scanner = new Scanner(System.in);
        while (true) {
            //set up and declare queue
            //sensory system
            String positionQueue = "Position_Sensor";
            String speedQueue = "Speed_Sensor";
            String cabinQueue = "Cabin_Sensor";
            String weatherQueue = "Weather_Sensor";
            //actuator system
            String enginesQueue = "Engines_Actuator";
            String wingFlapQueue = "Wing_Flap_Actuator";
            String tailsFlapQueue = "Tail_Flap_Actuator";
            String landingGearQueue = "Landing_Gear_Actuator";
            String oxygenMasksQueue = "Oxygen_Masks_Actuator";
            channel.queueDeclare(positionQueue,false,false,false,null);
            channel.queueDeclare(speedQueue,false,false,false,null);
            channel.queueDeclare(cabinQueue,false,false,false,null);
            channel.queueDeclare(weatherQueue,false,false,false,null);
//            channel.queueDeclare(enginesQueue,false,false,false,null);
//            channel.queueDeclare(wingFlapQueue,false,false,false,null);
//            channel.queueDeclare(tailsFlapQueue,false,false,false,null);
//            channel.queueDeclare(oxygenMasksQueue,false,false,false,null);
//            channel.queueDeclare(landingGearQueue,false,false,false,null);



//        channel.basicPublish("",altitudeQueue,null,altitudeQueue.getBytes());
//        System.out.println("Published message");

            //create the sensor and actuator objects
            PositionSensor positionSensor = new PositionSensor(channel, positionQueue,"Position Sensor");
            CabinSensor cabinSensor = new CabinSensor(channel, positionQueue,"Cabin Sensor");
            SpeedSensor speedSensor = new SpeedSensor(channel, speedQueue,"Speed Sensor");
            WeatherSensor weatherSensor = new WeatherSensor(channel, weatherQueue,"Weather Sensor");


            Thread positionSensorThread = new Thread(positionSensor);
            Thread cabinSensorThread = new Thread(cabinSensor);
            Thread speedSensorThread = new Thread(speedSensor);
            Thread weatherSensorThread = new Thread(weatherSensor);

            positionSensorThread.start();
            cabinSensorThread.start();
            speedSensorThread.start();
            weatherSensorThread.start();


            positionSensorThread.join();
            cabinSensorThread.join();
            speedSensorThread.join();
            weatherSensorThread.join();
            System.out.println("Press Enter to continue or Type 'exit' to quit.");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            // Process the input...
        }

        // close the channel and connection
        channel.close();
        connection.close();
        System.exit(0);

    }
}



//Data abstraction for Sensory system
abstract class BaseSensor implements Runnable{
    private final Channel channel;
    private final String queueName;
    private final String sensorName;

    BaseSensor(Channel channel, String queueName, String sensorName) {
        this.channel = channel;
        this.queueName = queueName;
        this.sensorName = sensorName;
    }

    abstract String getMessage();

    public String getSensorName() {
        return sensorName;
    }

    public void run() {
        int i=0;
        try {
            while (i<=0) {
                String message = getMessage();
                channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println("\t"+getSensorName());
                System.out.println("------------------------");
                System.out.println( message);
                System.out.println("========================\n");
                Thread.sleep(1000);
                i++;
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}


//Sensor class for position
class PositionSensor extends BaseSensor{
    PositionSensor(Channel channel, String queueName, String sensorName) {
        super(channel, queueName, sensorName);
    }

    @Override
    String getMessage() {
        // simulate data from position sensor
        int altitude = (int) (Math.random() * 50000);
        int longitude = (int) (Math.random() * 180);
        int latitude = (int) (Math.random() * 180);
        return "Altitude: " + altitude + "\nLongitude: " + longitude + "\nLatitude: " + latitude;
    }
}


//Sensor class for cabin
class CabinSensor extends BaseSensor{

    CabinSensor(Channel channel, String queueName, String sensorName) {
        super(channel, queueName, sensorName);
    }

    @Override
    String getMessage(){
        // simulate data from cabin sensor
        int temperature = (int) (Math.random() * 30);
        int pressure = (int) (Math.random() * 1000);
        return "Temperature: " + temperature + "\nPressure: " + pressure;
    }
}

//Sensor class for speed
class SpeedSensor extends BaseSensor{

    SpeedSensor(Channel channel, String queueName, String sensorName) {
        super(channel, queueName, sensorName);
    }

    @Override
    String getMessage() {
        // simulate data from speed sensor
        int speed = (int) (Math.random() * 1000);
        return "Speed: " + speed;
    }
}

//Sensor class for weather
class WeatherSensor extends BaseSensor{

    WeatherSensor(Channel channel, String queueName, String sensorName) {
        super(channel, queueName, sensorName);
    }

    @Override
    String getMessage() {
        // simulate data from weather sensor
        String[] conditions = {"Sunny", "Cloudy", "Rainy", "Snowy"};
        int index = (int) (Math.random() * conditions.length);
        String condition = conditions[index];
        return "Condition: " + condition;
    }
}





//Data abstraction for Actuator system
//abstract class BaseActuator implements Runnable {
//
//    private final Channel channel;
//    private final String queueName;
//    private final String actuatorName;
//
//    BaseActuator(Channel channel, String queueName, String actuatorName) {
//        this.channel = channel;
//        this.queueName = queueName;
//        this.actuatorName = actuatorName;
//    }
//
//    public abstract void run();
//
//    String getMessageFromQueue() throws IOException, InterruptedException {
//        // set up consumer
//        DefaultConsumer consumer = new DefaultConsumer(channel);
//        channel.basicConsume(queueName, true, consumer);
//
//        // wait for message from queue
//        DefaultConsumer.Delivery delivery ;
//        try {
//            delivery = consumer.nextDelivery();
//        } catch (IOException e) {
//            // handle the exception
//            System.err.println("Failed to retrieve message from queue: " + e.getMessage());
//            return null;
//        }
//        String message = new String(delivery.getBody(), "UTF-8");
//        System.out.println("Received message '" + message + "' by " + actuatorName);
//        return message;
//    }
//
//
//    String getActuatorName() {
//        return actuatorName;
//    }
//
//    void sendMessageToQueue(String message) throws IOException {
//        channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
//        System.out.println("Sent message '" + message + "' to " + actuatorName);
//    }
//}



//Actuator class for engine







//    abstract class BaseSensor implements Sensor {
//    private final Channel channel;
//    private final String queueName;
//    private final String sensorName;
//
//    BaseSensor(Channel channel, String queueName, String sensorName) {
//        this.channel = channel;
//        this.queueName = queueName;
//        this.sensorName = sensorName;
//    }
//
//    abstract String getMessage();
//
//    public String getSensorName() {
//        return sensorName;
//    }
//
//    public void run() {
//        try {
//            while (true) {
//                String message = getMessage();
//                channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
//                System.out.println("Sent " + message + " from " + getSensorName());
//                Thread.sleep(1000);
//            }
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//
//
//class AltitudeSensor extends BaseSensor {
//
//    AltitudeSensor(Channel channel, String queueName) {
//        super(channel, queueName, "altitude");
//    }
//
//    @Override
//    String getMessage() {
//        return "Altitude:" + Math.round(Math.random() * 10000);
//    }
//}
//
//
//class AirSpeedSensor extends BaseSensor {
//
//    AirSpeedSensor(Channel channel, String queueName) {
//        super(channel, queueName, "airspeed");
//    }
//
//    @Override
//    String getMessage() {
//        return "Airspeed: " + Math.round(Math.random() * 800);
//    }
//
//}
//
//class WeatherSensor extends BaseSensor {
//
//    WeatherSensor(Channel channel, String queueName) {
//        super(channel, queueName, "weather");
//    }
//
//    @Override
//    String getMessage() {
//        String[] conditions = {"Sunny", "Rainy", "Cloudy"};
//        return "Weather:" + conditions[(int) Math.round(Math.random() * 2)];
//    }
//}
//
//class PositionSensor extends BaseSensor {
//
//    PositionSensor(Channel channel, String queueName) {
//        super(channel, queueName, "position");
//    }
//
//    @Override
//    String getMessage() {
//        double latitude = Math.random() * 180 - 90;
//        double longitude = Math.random() * 360 - 180;
//        return "Position:" + latitude + "," + longitude;
//    }
//}
//
//abstract class BaseActuator implements Actuator {
//    private final Channel channel;
//    private final String queueName;
//    private final String actuatorName;
//
//    BaseActuator(Channel channel, String queueName, String actuatorName) {
//        this.channel = channel;
//        this.queueName = queueName;
//        this.actuatorName = actuatorName;
//    }
//
//    public String getActuatorName() {
//        return actuatorName;
//    }
//
//    public void run() {
//        try {
//            channel.queueDeclare(queueName, false, false, false, null);
//
//            //create a callback to receive messages from the flight control
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String message = new String(delivery.getBody(),StandardCharsets.UTF_8);
//                System.out.println("Received: " + message + "by" + getActuatorName());
//                activate(message);
//            };
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    abstract void activate(String message);
//
//}