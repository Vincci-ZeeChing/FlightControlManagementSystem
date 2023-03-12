package org.example;

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



        //set up and declare queue
        //sensory system
        String positionQueue = "Position_Sensor";
        String speedQueue = "Speed_Sensor";
        String cabinQueue = "Cabin_Sensor";
        String weatherQueue = "Weather_Sensor";
        //actuator system
        channel.queueDeclare(positionQueue,false,false,false,null);
        channel.queueDeclare(speedQueue,false,false,false,null);
        channel.queueDeclare(cabinQueue,false,false,false,null);
        channel.queueDeclare(weatherQueue,false,false,false,null);

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

    BaseSensor(Channel channel, String queueName, String sensorName) {
        this.channel = channel;
        this.queueName = queueName;
    }

    abstract String getMessage();

    public void run() {
        int i=0;
        try {
            while (i<=0) {
                String message = getMessage();
                channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println( message);
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
        String[] conditions = {"Sunny","Rainy", "Snowy"};
        int index = (int) (Math.random() * conditions.length);
        String condition = conditions[index];
        return "Condition: " + condition;
    }
}

