package o.kinectcapture;


// CapturePics.java
// Andrew Davison, December 2011, ad@fivedots.psu.ac.th

/* Show a sequence of images snapped from the Kinect
   Usage:
      > java CapturePics
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class CapturePics extends JFrame 
{
  private PicsPanel pp;

  public CapturePics()
  {
    super("Capture Pics");
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    pp = new PicsPanel(this); // the sequence of snaps appear here
    c.add( pp, "Center");

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { pp.closeDown();    // stop snapping pics
        System.exit(0);
      }
    });

    pack();  
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of CapturePics()


  public static void main( String args[] )
  {  new CapturePics();  }

} // end of CapturePics class
