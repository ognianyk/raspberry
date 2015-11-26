package ognianyk.pavel;

import jssc.*;

/**
 * Created by Pavel on 04.11.2015.
 */
public class GPSlogger {

    private static SerialPort serialPort;
    private static String line;
    private PortReader portReader;

    public static void main(String[] args) {
        //Передаём в конструктор имя порта
        for (String s : SerialPortList.getPortNames()) {
            System.out.println(s);
        }
        if (args.length > 0 && args[0] != null && !args[0].isEmpty()) {
            new GPSlogger().runGPS(args[0]);
        }
    }

    public NMEA.GPSPosition getPosition() {
        return portReader.getGpsPosition();
    }

    public void runGPS(String port) {
        serialPort = new SerialPort(port);
        try {
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры
            serialPort.setParams(SerialPort.BAUDRATE_4800,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //Устанавливаем ивент лисенер и маску
            portReader = new PortReader();
            serialPort.addEventListener(portReader);
            line = "";
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    private class PortReader implements SerialPortEventListener {

        private NMEA nmea = new NMEA();
        private NMEA.GPSPosition gpsPosition;

        public NMEA.GPSPosition getGpsPosition() {
            return gpsPosition;
        }

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString();

                    //  System.out.print(data);
                    if (data.startsWith("$")) {
                        if (!line.isEmpty()) {
                            gpsPosition = nmea.parse(line);
//                            System.out.println(gpsPosition.toString());
                        }
                        line = data;
                    } else {
                        line += data;
                    }
                    //И снова отправляем запрос
//                    serialPort.writeString("Get data");
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}