package f.ktransformer;
// ViewerPanel.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* The Kinect RGB image is modified so that the user (or users)
   are shown against a background image of a park.
   The background changing code is based on the ChangeBG example.

   The application applies an image filter to the camera image
   to change its appearance. The filter can be 
   changed by the user selecting a new op. Also the current filter
   may cycle through slight changes if it is updateable. 
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



public class ViewerPanel extends JPanel implements Runnable
{
  private static final String BACK_FNM = "parkBench.jpg";

  private BufferedImage backIm, cameraImage;
      /* the background image and final camera image (with only the users showing).
         The camera image will be built from the Kinect RGB image on each update,
         modified with the current filter op, and then drawn over the 
         static background image. 
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
  private int imWidth, imHeight;
  private Context context;
  private DepthMetaData depthMD;
  private ImageGenerator imageGen; 
  private DepthGenerator depthGen; 
  private SceneMetaData sceneMD;
      /* used to create a labeled user map, where each pixel holds a user ID
         (1, 2, etc.), or 0 to mean it is part of the background
      */

  private FilterOp fop;   // current filter



  public ViewerPanel(FilterOp startFop)
  {
    setBackground(Color.GRAY);
    fop = startFop;
    System.out.println("Using filter " + fop);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    backIm = loadImage(BACK_FNM);
    configOpenNI();

    imWidth = depthMD.getFullXRes();
    imHeight = depthMD.getFullYRes();
    System.out.println("Kinect imaging dimensions (" + imWidth + ", " +
                                                       imHeight + ")");
    setSize( new Dimension(imWidth, imHeight));   // to avoid frame gap

    hideBGPixel =  new Color(0, 0, 255, 0).getRGB();   // transparent blue

    // create d.s for holding camera pixels and image
    cameraPixels = new int[imWidth * imHeight];
    cameraImage =  new BufferedImage( imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
             // the image must have an alpha channel for the transparent blue pixels

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



  public void setFilter(FilterOp fp)
  // called from the top-level to change the current filter
  { fop = fp;  }


  public Dimension getPreferredSize()
  { return new Dimension(imWidth, imHeight); }


  public void run()
  // The wait for kinect / update / draw loop.
  {
    totalTime = 0;
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

      fop.update(totalTime);
      screenUsers();
      cameraImage = fop.apply(cameraImage);    
          // modify the camera image with the current filter op
      imageCount++;

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
    cameraImage.setRGB(0, 0, imWidth, imHeight, cameraPixels, 0, imWidth);
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
    int rowLen = imWidth * 3;    // number of bytes in each row
    for (int row = 0; row < imHeight; row++) {
	    bbIdx = rowStart;
      // System.out.println("bbIdx: " + bbIdx);
	    for (int col = 0; col < imWidth; col++) {
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
     thereby making it transparent
  */
  {
    depthMD = depthGen.getMetaData();    // reassignment to avoid a flickering viewpoint

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




  public void paintComponent(Graphics g)
  // draw background then the modified camera image
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    g2.drawImage(backIm, 0, 0, this);
    g2.drawImage(cameraImage, 0, 0, this);

    writeStats(g2);
  } // end of paintComponent()




  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
	g2.setColor(Color.YELLOW);
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

