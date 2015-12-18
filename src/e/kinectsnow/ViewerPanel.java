package e.kinectsnow;
// ViewerPanel.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* The Kinect RGB image is modified so that the user (or users) appear to
   be in a snowy landscape. Snow starts falling, and will tend to pile up on
   top of the user until he moves out of the way.

   The background changing code is based on the ChangeBG example.

   The snow is managed by a SnowManager object, and its animation is controlled
   by a timer which triggers periodic updates.
*/

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.io.*;
import javax.imageio.*;
import java.util.*;

import org.OpenNI.*;

import java.nio.*;



public class ViewerPanel extends JPanel implements Runnable, ActionListener
{
  private static final String BACK_FNM = "snowyRoad.jpg";


  private BufferedImage backIm, cameraImage;
      /* the background image and final camera image (with only the users showing).
         The camera image will be built from the Kinect RGB image on each update,
         and then drawn over the static background image. 
      */

  private int[] cameraPixels;
      // holds the pixels that will fill the cameraImage image

  private int hideBGPixel;
     /* the "hide the background" pixel: this could be any colour 
        so long as its alpha value is 0 */

  private volatile boolean isRunning;
  
  // used for the average ms processing information
  private int imageCount = 0;
  private long totalTime = 0;
  private DecimalFormat df;
  private Font msgFont;

  // OpenNI
  private int pWidth, pHeight;    // of Kinect panel
  private Context context;
  private DepthMetaData depthMD;
  private ImageGenerator imageGen;
  private DepthGenerator depthGen; 

  private SceneMetaData sceneMD;
      /* used to create a labeled user map, where each pixel holds a user ID
         (1, 2, etc.), or 0 to mean it is part of the background */

  // for snow animation
  private javax.swing.Timer animatorTimer;
  private SnowManager snowMan;
  private volatile boolean moveSnow;     
       // used to flag that it's time for a snow update



  public ViewerPanel()
  {
    setBackground(Color.WHITE);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    backIm = loadImage(BACK_FNM);
    configOpenNI();

    pWidth = depthMD.getFullXRes();
    pHeight = depthMD.getFullYRes();
    System.out.println("Kinect imaging dimensions (" + pWidth + ", " +
                                                       pHeight + ")");
    setSize( new Dimension(pWidth, pHeight));   // to avoid frame gap

    hideBGPixel =  new Color(0, 0, 255, 0).getRGB();   // transparent blue

    // create d.s for holding camera pixels and image
    cameraPixels = new int[pWidth * pHeight];
    cameraImage =  new BufferedImage( pWidth, pHeight, BufferedImage.TYPE_INT_ARGB);
             // the image must have an alpha channel for the transparent blue pixels

    snowMan =  new SnowManager(pWidth, pHeight, this);

    // create a timer for animating the falling snow
    moveSnow = false;
	animatorTimer = new javax.swing.Timer(75, this);    // refresh rate
    animatorTimer.setInitialDelay(500);       // wait time before start
    animatorTimer.setCoalesce(true);	
    animatorTimer.start();                    // start the timer

    new Thread(this).start();   // start updating the panel's image
  } // end of ViewerPanel()



  private BufferedImage loadImage(String imFnm)
  {
    BufferedImage image = null;
    try {
      image = ImageIO.read( new File(imFnm));
      System.out.println("Loaded " + imFnm);
    }
    catch (IOException e) 
    {  System.out.println("Unable to load " + imFnm);  }
    return image;
  }  // end of loadImage()



  private void configOpenNI()
  // create context, depth generator, image generator,
  // user generator, scene metadata
  {
    try {
      context = new Context();
      
      // add the NITE License 
      License license = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");   // vendor, key
      context.addLicense(license); 
      
      depthGen = DepthGenerator.create(context);
      imageGen = ImageGenerator.create(context);

      // set the viewpoint of the DepthGenerator to match the ImageGenerator
      boolean hasAltView = 
         depthGen.isCapabilitySupported("AlternativeViewPoint");   // returns true
      if (hasAltView) { 
        AlternativeViewpointCapability altViewCap =
                  depthGen.getAlternativeViewpointCapability();
        altViewCap.setViewpoint(imageGen); 
      }
      else {
        System.out.println("Alternative ViewPoint not supported"); 
        System.exit(1);
      }

      MapOutputMode mapMode = new MapOutputMode(640, 480, 30);   // xRes, yRes, FPS
      depthGen.setMapOutputMode(mapMode); 
      imageGen.setMapOutputMode(mapMode); 
      imageGen.setPixelFormat(PixelFormat.RGB24);
      
      // set Mirror mode for all 
      context.setGlobalMirror(true);

      depthMD = depthGen.getMetaData();
           // use depth metadata to access depth info (avoids bug with DepthGenerator)

      UserGenerator userGen = UserGenerator.create(context);
      sceneMD = userGen.getUserPixels(0);
         // used to return a map containing user IDs (or 0) at each depth location

      context.startGeneratingAll(); 
      System.out.println("Started context generating..."); 
    } 
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }  // end of configOpenNI()



  public Dimension getPreferredSize()
  { return new Dimension(pWidth, pHeight); }


  public void run()
  /* Update and display the users whenever the context is updated. 
     Animate the snow when necessary.
  */
  {
    isRunning = true;
    while (isRunning) {
      try {
        context.waitAndUpdateAll();  
            // wait for all nodes to have new data, then updates them
      }
      catch(StatusException e)
      {  System.out.println(e); 
         System.exit(1);
      }
	  long startTime = System.currentTimeMillis();
      screenUsers();

      if (moveSnow) {    // time to animate the snow
        moveSnow = false;
        snowMan.update();
      }

      totalTime += (System.currentTimeMillis() - startTime);
      repaint();
    }

    // close down
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    System.exit(0);
  }  // end of run()



  public void closeDown()
  {  isRunning = false;  } 


  public void actionPerformed(ActionEvent e)
  // the flag causes the snow to be animated in the next iteration of the run loop 
  {  moveSnow = true;  }



  private void screenUsers()
  {
    // store the Kinect RGB image as a pixel array in cameraPixels
    try {
      ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
      convertToPixels(imageBB, cameraPixels);
    }
    catch (GeneralException e) {
      System.out.println(e);
    }

    hideBackground(cameraPixels);

    // change the modified pixels into an image
    cameraImage.setRGB( 0, 0, pWidth, pHeight, cameraPixels, 0, pWidth );
    imageCount++;
  }  // end of screenUsers()




  private void convertToPixels(ByteBuffer pixelsRGB, int[] cameraPixels)
  /* Transform the ByteBuffer of pixel data into a pixel array
     Converts RGB bytes to ARGB ints with no transparency. 
  */
  {
    int rowStart = 0;
        // rowStart will index the first byte (red) in each row;
        // starts with first row, and moves down

    int bbIdx;               // index into ByteBuffer
    int i = 0;               // index into pixels int[]
    int rowLen = pWidth * 3;    // number of bytes in each row
    for (int row = 0; row < pHeight; row++) {
	    bbIdx = rowStart;
      // System.out.println("bbIdx: " + bbIdx);
	    for (int col = 0; col < pWidth; col++) {
	      int pixR = pixelsRGB.get( bbIdx++ );
	      int pixG = pixelsRGB.get( bbIdx++ );
	      int pixB = pixelsRGB.get( bbIdx++ );
	      cameraPixels[i++] = 
	         0xFF000000 | ((pixR & 0xFF) << 16) | 
	         ((pixG & 0xFF) << 8) | (pixB & 0xFF);
	    }
      rowStart += rowLen;   // move to next row
    }
  }  // end of convertToPixels()



  private void hideBackground(int[] cameraPixels)
  /* assign the "hide BG" value to any image pixels used for non-users
     thereby making it transparent */
  {
    depthMD = depthGen.getMetaData();    // reassignment to avoid a flickering viewpoint

    // ShortBuffer depth = depthMD.getData().createShortBuffer();
         // update the depth map

    // update the user ID map
    ShortBuffer usersBuf = sceneMD.getData().createShortBuffer();
      /* each pixel holds an user ID (e.g. 1, 2, 3), or 0 to 
         denote that the pixel is part of the background.  */

    while (usersBuf.remaining() > 0) {
      int pos = usersBuf.position();
      short userID = usersBuf.get();
      if (userID == 0) // if not a user (i.e. is part of the background)
        cameraPixels[pos] = hideBGPixel;   // make pixel transparent
    }
  }  // end of hideBackground()


  // ----------- snow related --------------------


  public boolean onUser(int x, int y)
  // called by SnowFlakes manager to determine if a flake at (x,y) is on a user
  {
    if ((x < 0) || (x >= pWidth) ||
        (y < 0) || (y >= pHeight)) 
      return false;

    int pixel = cameraPixels[(pWidth*y) + x];
    return (pixel != hideBGPixel);
  }  // end of onUser()

 
  // ---------- drawing --------------------------

  public void paintComponent(Graphics g)
  // draw background, the camera image, and the snow
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    g2.drawImage(backIm, 0, 0, this);
    g2.drawImage(cameraImage, 0, 0, this);

    snowMan.draw(g2);

    writeStats(g2);
  } // end of paintComponent()




  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
	g2.setColor(Color.BLUE);
    g2.setFont(msgFont);
    int panelHeight = getHeight();
    if (imageCount > 0) {
      double avgGrabTime = (double) totalTime / imageCount;
	  g2.drawString("Pic " + imageCount + "  " +
                   df.format(avgGrabTime) + " ms", 
                   5, panelHeight-10);  // bottom left
    }
    else  // no image yet
	  g2.drawString("Loading...", 5, panelHeight-10);
  }  // end of writeStats()


} // end of ViewerPanel class

