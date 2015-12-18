package j.motorcontrol;
// MotorTest.java
// Andrew Davison, ad@fivedots.coe.psu.ac.th, October 2011

/* Class for opening and closing the Kinect Sensor's motor
   USB info from  http://openkinect.org/wiki/USB_Devices
*/

import java.io.*;
import ch.ntb.usb.*;


public class MotorTest
{
  private static final short VENDOR_ID = (short)0x045e;
  private static final short PRODUCT_ID = (short)0x02b0;
          // the IDs were obtained by looking at the kinect motor using USBDeview


  public static void main(String[] args)
  {

    // find the device using the vendor and product IDs
    Device dev = USB.getDevice(VENDOR_ID, PRODUCT_ID);
    if (dev == null) {
      System.out.println("Device not found");
      System.exit(1);
    }

    System.out.println("Opening device");
    try {
      dev.open(1, 0, -1); 
      // open device with configuration 1, interface 0 and no alt interface
      System.out.println("Opened device");
    }
    catch (USBException e) {
      System.out.println(e);
      System.exit(1);
    }

    System.out.println("Closing device");
    try {
      if (dev != null)
        dev.close();
    }
    catch (USBException e) {
      System.out.println(e);
    }
  }  // end of main()
	

}  // end of MotorTest class
