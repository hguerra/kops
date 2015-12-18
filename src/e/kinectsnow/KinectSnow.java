package e.kinectsnow;


// KinectSnow.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* The Kinect RGB image is modified so that the user (or users) appear to
   be in a snowy landscape. Snow starts falling, and will tend to pile up on
   top of the user until he moves out of the way.

   Usage:
      > java KinectSnow
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class KinectSnow extends JFrame 
{

  private ViewerPanel viewerPanel;


  public KinectSnow()
  {
    super("Kinect Snow");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    viewerPanel = new ViewerPanel();
    c.add( viewerPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { viewerPanel.closeDown();    // stop showing images
      }
    });

    setResizable(false);   // must be before pack(), or size is wrong
    pack();  

    setLocationRelativeTo(null);
    setVisible(true);
  } // end of KinectSnow()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  new KinectSnow(); }  

} // end of KinectSnow class
