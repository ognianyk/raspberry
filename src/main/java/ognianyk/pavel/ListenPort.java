package ognianyk.pavel;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioInterruptCallback;
import com.pi4j.wiringpi.GpioUtil;

/**
 * Created by Pavel on 16.11.2015.
 */
public class ListenPort {


    public static int RCSWITCH_MAX_CHANGES = 800;

    public static int[] timings = new int[RCSWITCH_MAX_CHANGES];

    public static int nReceivedValue = 0;
    public static int nReceivedBitlength = 0;
    public static int nReceivedDelay = 0;
    public static int nReceivedProtocol = 0;
    public static int nReceiveTolerance = 120;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("<--Pi4J--> GPIO interrupt test program");

        // setup wiringPi
        if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
            return;
        }

        int i = 0;
        System.out.println("<--Pi4J--> GPIO port " + i);
        GpioUtil.export(RaspiPin.GPIO_00.getAddress(), GpioUtil.DIRECTION_IN);
        GpioUtil.setEdgeDetection(RaspiPin.GPIO_00.getAddress(), GpioUtil.EDGE_BOTH);
        Gpio.pinMode(i, Gpio.INPUT);
        Gpio.wiringPiISR(0, Gpio.INT_EDGE_BOTH, new GpioInterruptCallback() {
            int duration;
            int changeCount = 0;
            int lastTime = 0;
            int repeatCount = 0;

            public void callback(int pin) {
                process();
            }

            private synchronized void process() {
//                System.out.println("time " + System.nanoTime() / 100);
//
//                System.out.println("INTERRUPT");
                int time = (int) (System.nanoTime() / 1000);
                duration = time - lastTime;


                if (duration > 5000 && duration > (timings[0] - 200) && duration < (timings[0] + 200)) {

                    repeatCount++;
                    changeCount--;

                    if (repeatCount == 2) {
                        if (receiveProtocol1(changeCount) == false) {
//        	        if (receiveProtocol2(changeCount) == false){
//        	          //if (receiveProtocol3(changeCount) == false){}
//        	        }
                        }

                        repeatCount = 0;
                    }
                    changeCount = 0;
                } else if (duration > 5000) {
                    changeCount = 0;
                }

                if (changeCount >= RCSWITCH_MAX_CHANGES) {
                    System.out.println("RCSWITCH_MAX_CHANGES");
                    changeCount = 0;
                    repeatCount = 0;
                }
                timings[changeCount++] = duration;

                lastTime = time;


            }

            private boolean receiveProtocol1(int changeCount) {

                String binCode = "";
                long delay = timings[0] / 31;
                long delayTolerance = (long) (delay * nReceiveTolerance * 0.01);


                for (int i = 1; i < changeCount; i = i + 2) {
                    if (timings[i] > delay - delayTolerance && timings[i] < delay + delayTolerance && timings[i + 1] > delay * 3 - delayTolerance && timings[i + 1] < delay * 3 + delayTolerance) {
                        binCode += "0";
                    } else if (timings[i] > delay * 3 - delayTolerance && timings[i] < delay * 3 + delayTolerance && timings[i + 1] > delay - delayTolerance && timings[i + 1] < delay + delayTolerance) {
                        binCode += "1";
                    } else {
                        // Failed
                        i = changeCount;
                        binCode = "";
                    }
                }

                if (changeCount > 6 && binCode != "") {    // ignore < 4bit values as there are no devices sending 4bit values => noise

                    nReceivedValue = Integer.parseInt(binCode, 2);
                    nReceivedBitlength = changeCount / 2;
                    nReceivedDelay = (int) delay;
                    nReceivedProtocol = 1;

                    System.out.println();
                    System.out.println(nReceivedValue + "\t" + binCode + "\t" + nReceivedBitlength + "\t" + changeCount);
                    for (int i = 1; i < changeCount; i++) {
                        System.out.print(timings[i] + " ");
                    }
                    System.out.println();
                    System.out.println();

                }


                if (binCode == "") {
                    return false;
                } else if (binCode != "") {
                    return true;
                }
                return false;


            }
        });


        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");
        while (true) {
//      if (nReceivedValue != 0)
//      {
//        System.out.println("nReceivedValue: " + nReceivedValue);
//
//        nReceivedValue = 0;
//      }

//      Thread.sleep(300);
        }
    }
}