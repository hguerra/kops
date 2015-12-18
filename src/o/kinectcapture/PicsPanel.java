package o.kinectcapture;
// PicsPanel.java
// Andrew Davison, December 2011, ad@fivedots.psu.ac.th

/* Take a picture every few ms, and show in the panel.
   The delay value is obtained from KinectCapture.

   The snaps are taken by a thread, which is terminated when the
   isRunning boolean is set to true by closeDown(). closeDown()
   blocks until run() has called KinectCapture.close() and finished. 
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.io.*;
import javax.imageio.*;


class PicsPanel extends JPanel implements Runnable
{
  private static final int DELAY = 35;  // ms or 140ms for high-res

  private static final Dimension PANEL_SIZE = new Dimension(200, 100);  
          // dimensions of panel initially;  later set to video's frame size

  private JFrame top;
  private BufferedImage image = null;
  private KinectCapture camera; 
  private volatile boolean isRunning;
  
  // used for the average ms snap time info
  private int imageCount = 0;
  private long totalTime = 0;
  private DecimalFormat df;
  private Font msgFont;


  public PicsPanel(JFrame top)
  {
    this.top = top;
    setMinimumSize(PANEL_SIZE);
    setPreferredSize(PANEL_SIZE);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    new Thread(this).start();   // start updating the panel's image
  } // end of PicsPanel()



  public void run()
  // take a picture every delay ms
  {
    camera = new KinectCapture();    // normal resolution
    // camera = new KinectCapture(Resolution.HIGH);

    // update panel and window sizes to fit video's frame size
    Dimension frameSize = camera.getFrameSize();
    if (frameSize != null) {
      setMinimumSize(frameSize);
      setPreferredSize(frameSize);
      top.pack();   // resize and center JFrame
      top.setLocationRelativeTo(null);
    }

    long duration;
    BufferedImage im = null;
    isRunning = true;
    System.out.println("Snap delay: " + DELAY + "ms");

    while (isRunning) {
	    long startTime = System.currentTimeMillis();
      im = camera.getImage();  // take a snap

      duration = System.currentTimeMillis() - startTime;

      if (im == null)
        System.out.println("Problem loading image " + (imageCount+1));
      else {
        image = im;   // only update image if im contains something
        imageCount++;
        totalTime += duration;
        repaint();
      }

      if (duration < DELAY) {
        try {
          Thread.sleep(DELAY-duration);  // wait until delay time has passed
        } 
        catch (Exception ex) {}
      }
    }

    camera.close();    // close down the camera
  }  // end of run()




  public void paintComponent(Graphics g)
  /* Draw the snap and add the average ms snap time at the 
     bottom of the panel. */
  { 
    super.paintComponent(g);

    int panelHeight = getHeight();
    g.setColor(Color.GRAY);    // gray background
    g.fillRect(0, 0, getWidth(), panelHeight);

    // center the image
    int x = 0;
    int y = 0;
    if (image != null) {
      x = (int)(getWidth() - image.getWidth())/2;
      y = (int)(panelHeight - image.getHeight())/2;
    }
    g.drawImage(image, x, y, this);   // draw the snap

    // write statistics in bottom-left corner
	  g.setColor(Color.YELLOW);
    g.setFont(msgFont);
    if (imageCount > 0) {
      double avgGrabTime = (double) totalTime / imageCount;
	  g.drawString("Pic " + imageCount + "  " +
                   df.format(avgGrabTime) + " ms", 
                   5, panelHeight-10);  // bottom left
    }
    else  // no image yet
	    g.drawString("Loading...", 5, panelHeight-10);
  } // end of paintComponent()



  public void closeDown()
  /* Terminate run() and wait for the camera to be closed.
     This stops the application from exiting until everything
     has finished. */
  { 
    isRunning = false;
    while (!camera.isClosed()) {
      try {
        Thread.sleep(DELAY);   // wait a while
      } 
      catch (Exception ex) {}
    }
  } // end of closeDown()


} // end of PicsPanel class

