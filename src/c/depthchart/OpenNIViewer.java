package c.depthchart;


// OpenNIViewer.java
// Andrew Davison, September 2012, ad@fivedots.psu.ac.th

/* Different OpenNI viewers are implemented in different 
   versions of the ViewerPanel class in Chapter 2.

   This one displays a grayscale depth image and a depth count chart
   drawn using JFreeChart
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class OpenNIViewer extends JFrame 
{
  private ViewerPanel viewerPanel;


  public OpenNIViewer()
  {
    super("OpenNI Viewer Example");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    viewerPanel = new ViewerPanel();
    c.add( viewerPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { viewerPanel.closeDown();    // stop showing images
        // System.exit(0);
      }
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of OpenNIViewer()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  new OpenNIViewer();  }

} // end of OpenNIViewer class
