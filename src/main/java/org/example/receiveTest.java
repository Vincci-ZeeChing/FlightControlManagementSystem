package org.example;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class receiveTest {
    public final static String ALTITUDE_SENSOR_QUEUE = "Altitude_Sensor";
    public final static String SPEED_SENSOR_QUEUE = "Speed_Sensor";
    public final static String CABIN_SENSOR_QUEUE = "Cabin_Sensor";
    public final static String WEATHER_SENSOR_QUEUE = "Weather_Sensor";

    public static void main(String[] args) throws IOException, TimeoutException {
        // create RabbitMQ connection and channel
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Connection connection = null;
        Channel channel = null;

        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            // set up and declare channel
            channel.queueDeclare(ALTITUDE_SENSOR_QUEUE, false, false, false, null);
            channel.queueDeclare(SPEED_SENSOR_QUEUE, false, false, false, null);
            channel.queueDeclare(CABIN_SENSOR_QUEUE, false, false, false, null);
            channel.queueDeclare(WEATHER_SENSOR_QUEUE, false, false, false, null);

            // consume or retrieve the message
            channel.basicConsume(ALTITUDE_SENSOR_QUEUE, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);
            }, consumerTag -> {
                // handle cancellation
                System.out.println("Consumer cancelled: " + consumerTag);
            });

            channel.basicConsume(SPEED_SENSOR_QUEUE, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);
            }, consumerTag -> {
                // handle cancellation
                System.out.println("Consumer cancelled: " + consumerTag);
            });

            channel.basicConsume(CABIN_SENSOR_QUEUE, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);
            }, consumerTag -> {
                // handle cancellation
                System.out.println("Consumer cancelled: " + consumerTag);
            });

            channel.basicConsume(WEATHER_SENSOR_QUEUE, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(message);
            }, consumerTag -> {
                // handle cancellation
                System.out.println("Consumer cancelled: " + consumerTag);
            });


            System.out.println("Consumer started");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
