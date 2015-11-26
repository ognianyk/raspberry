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
        //������� � ����������� ��� �����
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
            //��������� ����
            serialPort.openPort();
            //���������� ���������
            serialPort.setParams(SerialPort.BAUDRATE_4800,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //�������� ���������� ���������� �������
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                    SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //������������� ����� ������� � �����
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
                    //�������� ����� �� ����������, ������������ ������ � �.�.
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
                    //� ����� ���������� ������
//                    serialPort.writeString("Get data");
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}