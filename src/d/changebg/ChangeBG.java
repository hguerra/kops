package d.changebg;


// ChangeBG.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* The Kinect RGB image is modified so that only the user (or users)
   are shown. The other pixels (the background) are made
   transparent, so that the background image specified on the 
   command line is visible.

   Usage:
      > run ChangeBG <background image filename>
    e.g.
      > run ChangeBG parkBench.jpg
      > run ChangeBG snowyRoad.jpg
      > run ChangeBG city.png
            -- all the images are 640x480 pixels large to fit the Kinect image size

    I recommend crouching on a chair with the city background -- the chair will disappear
    into the background, and you'll end up on the mat.
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class ChangeBG extends JFrame 
{
  private ViewerPanel viewerPanel;


  public ChangeBG(String backFnm)
  {
    super("Change Background");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    viewerPanel = new ViewerPanel(backFnm);
    c.add( viewerPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { viewerPanel.closeDown();  }  // stop showing images
    });

    setResizable(false);   // must be before pack(), or size is wrong
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of ChangeBG()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  
    if (args.length != 1)
      System.out.println("Usage: run ChangeBG <background image file>");
    else
      new ChangeBG(args[0]);  
  }  // end of main()

} // end of ChangeBG class
