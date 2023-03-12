package org.example;


import java.util.Timer;
import java.util.TimerTask;

import com.rabbitmq.client.*;

import java.io.IOException;


public class SensorSubsystem {
    public final static String SENSOR_QUEUE_NAME = "sensor_data";
    private final static String ACTUATOR_QUEUE_NAME = "actuator_data";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(SENSOR_QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(ACTUATOR_QUEUE_NAME, false, false, false, null);


        Timer timer = new Timer();
        timer.schedule(new SensorDataUpdater1(channel), 0, 1000);

        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received sensor data: " + message);

                // make decisions based on the sensor data
                int altitude = 0;
                int cabinPressure = 0;
                int speed = 0;
                int direction = 0;
                String weatherConditions = "";

                String[] data = message.split(",");
                if (data.length == 5) {
                    altitude = Integer.parseInt(data[0]);
                    cabinPressure = Integer.parseInt(data[1]);
                    speed = Integer.parseInt(data[2]);
                    direction = Integer.parseInt(data[3]);
                    weatherConditions = data[4];
                }

                // example decision-making logic
                boolean reduceSpeed = false;
                boolean increaseSpeed = false;
                boolean changeAltitude = false;
                if (cabinPressure < 500) {
                    // sudden loss of cabin pressure
                    reduceSpeed = true;
                    changeAltitude = true;
                } else {
                    if (speed > 500) {
                        reduceSpeed = true;
                    } else if (speed < 100) {
                        increaseSpeed = true;
                    }
                    if (altitude > 35000) {
                        changeAltitude = true;
                    }
                }

                // send commands to the actuator system through the RabbitMQ queue
                try {
                    String command = (reduceSpeed ? "reduce_speed" : "") + "," + (changeAltitude ? "change_altitude" : "");
                    channel.basicPublish("", ACTUATOR_QUEUE_NAME, null, command.getBytes());
                    System.out.println("Sent command: " + command);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        channel.basicConsume(SENSOR_QUEUE_NAME, true, consumer);
    }
}

class SensorDataUpdater1 extends TimerTask {
    private Channel channel;

    public SensorDataUpdater1(Channel channel) {
        this.channel = channel;
    }

    public void run() {
        // get updated sensor data every 3 seconds
        int altitude = getAltitude();
        int cabinPressure = getCabinPressure();
        int speed = getSpeed();
        int direction = getDirection();
        String weatherConditions = getWeatherConditions();

        // send the updated data to the decision system through the RabbitMQ queue
        try {
            String message = altitude + "," + cabinPressure + "," + speed + "," + direction + "," + weatherConditions;
            channel.basicPublish("", SensorSubsystem.SENSOR_QUEUE_NAME, null, message.getBytes());
            System.out.println("Sent sensor data: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAltitude() {
        // return the altitude reading
        int altitude = (int) (Math.random() * 50000);
        return altitude;
    }

    private int getCabinPressure() {
        // return the cabin pressure reading
        int pressure = (int) (Math.random() * 1000);
        return pressure;
    }

    private int getSpeed() {
        // return the speed reading
        int speed = (int) (Math.random() * 1000);
        return speed;
    }

    private int getDirection() {
        // return the direction reading
        return 0;
    }

    public String getWeatherConditions() {
        // return the weather conditions
        String[] conditions = {"Sunny", "Rainy", "Snowy"};
        int index = (int) (Math.random() * conditions.length);
        String condition = conditions[index];
        return condition;    }
}

