package m.breakout;
// BricksManager.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* Manages the creation of the bricks, which have randomly assigned
   lego brick images; all the images are the same size.

   The brick's positions, spaced out over the screen, is determined by
   BricksManager. There are NUM_ROWS rows of bricks.

   BricksManager also manages collision detection with the ball, and
   draws the bricks . When a ball is hit, it is deactivated so it is no longer
   drawn.

   There is no use made of update()
*/

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


public class BricksManager
{
  private static final int NUM_ROWS = 4;

  private static final String IM_DIR = "images/";
  private static final String[] LEGO_FNMS = {      // lego brick images
       "orangeBrick.png", "redBrick.png", "yellowBrick.png", 
       "blueBrick.png",  "grayBrick.png", "greenBrick.png" };

  private Brick[] bricks;
  private int numRemaining;    
         // the number of active bricks (i.e. the ones that are visible)



  public BricksManager(Dimension scrDim)
  {
    Dimension brickDim = getBrickSize(LEGO_FNMS[0]);
    if (brickDim == null) {
      System.out.println("No brick dimensions found in " + LEGO_FNMS[0]);
      System.exit(1);
    }
    int brickWidth = brickDim.width;
    int brickHeight = brickDim.height;

    // create NUM_ROWS of bricks, spaced out across the x-axis

    int xDist = brickWidth*4/3;   
                  // x-axis distance from start of one brick to start of next
    int numCols = (scrDim.width/xDist)-1; 

    int xGap = (scrDim.width - (numCols * xDist) + brickWidth/3)/2;
          // gap between bricks, so columns are equally spaced out from sides

    bricks = new Brick[NUM_ROWS*numCols];
    numRemaining = NUM_ROWS*numCols;

    Random rand = new Random();    // for randomly choosing an image fnm
    // NUM_ROWS*numCols bricks created
    int count = 0;
    for (int i = 0; i < NUM_ROWS; i++)
      for (int j = 0; j < numCols; j++) {
        int idx = rand.nextInt(LEGO_FNMS.length);
        bricks[count] = new Brick(LEGO_FNMS[idx], xGap + j*xDist, 
                                  70 + i*(brickHeight*3/2), scrDim);
        count++;
      }
  }  // end of BricksManager()



  private Dimension getBrickSize(String fnm)
  // return the dimensions of the brick image in IM_DIR
  {
    try {
      BufferedImage im = ImageIO.read( new File(IM_DIR + fnm));
      return new Dimension(im.getWidth(), im.getHeight());
    } 
    catch (IOException e) 
    {  return null;  }
  }  // end of getBrickSize()



  public int numRemaining()
  {  return numRemaining; }



  public Brick hitBy(Ball ball)
  // has the ball hit any of the bricks?
  {
    for(Brick b : bricks)
      if (b.isActive() && ball.hasHit(b)) {
        b.setActive(false);    // brick will no longer be drawn
        numRemaining--;
        return b;    // return reference to brick that was hit
      }
    return null;
  }  // end of hitBy()




  public void draw(Graphics g)
  { for(Brick b : bricks)
      b.draw(g);
  }

}  // end of BricksManager class

