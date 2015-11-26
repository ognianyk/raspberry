package ognianyk.pavel;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.mandfer.dht11.DHT11SensorReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Pavel on 30.10.2015.
 */
public class MeteoServer extends AbstractHandler {

    private String msg;
    private Date date;
    private GPSlogger gpSlogger = new GPSlogger();
    DHT11SensorReader sensor = new DHT11SensorReader();

    public MeteoServer() {
        gpSlogger.runGPS("/dev/ttyUSB0");
    }

    public synchronized void handle(String target,
                                    Request baseRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        if (date == null || (new Date()).getTime() - date.getTime() > 5000) {

            sensor.setdTHPIN(7);
            float[] readData = sensor.readData();
            date = new Date();
            msg = String.format("Температура: %.1f °C, Влажность: %.1f %%", readData[0], readData[1]);

        }
        if (gpSlogger.getPosition().fixed) {
            response.getWriter().println("<h3>" + gpSlogger.getPosition().toString() + "</h3>");
        }
        response.getWriter().println("<h3>" + msg + "</h3>");

    }

    public static void main(String[] args) {
        try {

            Server server = new Server(80);

            server.setHandler(new MeteoServer());
            server.start();
            server.join();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
