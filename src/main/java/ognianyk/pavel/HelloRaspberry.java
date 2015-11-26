package ognianyk.pavel;

import org.mandfer.dht11.DHT11SensorReader;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Pavel on 29.10.2015.
 */
public class HelloRaspberry {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        // display a few of the available system information properties
        for(int i=0;i<50;i++) {
            DHT11SensorReader sensor = new DHT11SensorReader();
            sensor.setdTHPIN(7);
            float[] readData = sensor.readData();
            String msg = String.format("temp: %.1f, hum %.1f", readData[0], readData[1]);
            System.out.println(msg);
            Thread.sleep(5000);
        }
    }
}
