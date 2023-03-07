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
    private final static String ACTUATOR_QUEUE_NAME = "Actuator_Data";


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

        // actuator system
//        channel.queueDeclare(ACTUATOR_QUEUE_NAME,false,false,false,null);

        // heading
        System.out.println("--------------------------------");
        System.out.println("     Flight Control System");
        System.out.println("--------------------------------\n");


        // update data every 5 second
        Timer timer = new Timer();
        timer.schedule(new SensorDataUpdater(channel), 0, 5000);

        Consumer consumer = new DefaultConsumer(channel) {
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received sensor data: \n" + message);

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

//                // send commands to the actuator system through the RabbitMQ queue
//                try {
//                    String command = (reduceSpeed ? "reduce_speed" : "") + "," + (changeAltitude ? "change_altitude" : "");
//                    channel.basicPublish("", ACTUATOR_QUEUE_NAME, null, command.getBytes());
//                    System.out.println("Sent command: " + command);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        };

        channel.basicConsume(ALTITUDE_SENSOR_QUEUE,true, consumer);
        channel.basicConsume(SPEED_SENSOR_QUEUE,true, consumer);
        channel.basicConsume(CABIN_SENSOR_QUEUE,true, consumer);
        channel.basicConsume(WEATHER_SENSOR_QUEUE,true, consumer);

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

    private String getAltitude() {
        // simulate data from altitude sensor
        int altitude = new Random().nextInt(25000) + 20000; // generate a random number between 25000 and 45000
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
        // code to get actual cabin temperature from sensors
        double temperature = new Random().nextDouble(20) + 10;
        return temperature;
    }

    public double getCabinPressureValue() {
        // code to get actual cabin pressure from sensors
        double pressure = new Random().nextDouble(1) + 12;
        return pressure;
    }

    private int getOxygenLevel() {
        double pressure = getCabinPressureValue();
        int oxygen = 0;
        if (pressure <= 2.7) {
            oxygen = (int) (new Random().nextDouble(20) + 1);
        } else if (pressure > 2.7 && pressure <= 10) {
            oxygen = (int) (new Random().nextDouble(21) + 50);
        } else if (pressure > 10) {
            oxygen = (int) (new Random().nextDouble(30) + 71);
        }
        return oxygen;
    }

    private String getSpeed() {
        // simulate data from speed sensor
        int speed = (int) (Math.random() * 1000);
        String speedStr = String.valueOf(speed);
        return "\tSpeed Sensor"+
                "\nSpeed: " + speedStr + " Kmh" +
                "\n----------------------";
    }

    private String getWeatherConditions() {
        // simulate data from weather sensor
        String[] conditions = {"Sunny", "Cloudy", "Rainy", "Snowy"};
        int index = (int) (Math.random() * conditions.length);
        String condition = conditions[index];
        return "\tWeather Sensor"+
                "\nCondition: " + condition +
                "\n----------------------";
    }
}