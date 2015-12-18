package e.kinectsnow;
// SnowManager.java
// Andrew Davison, April 2012, ad@fivedots.coe.psu.ac.th

/* Manages the snowflakes. 
   They start at the top of the screen, gradually drop, but may be stopped by
   the top-edge of a user. When a flake drops off the bottom of the screen it becomes invisible,
   and is reused by being placed back at the top of the screen somewhere.

   The flakes start falling in random batches of size START_BATCH to make
   the snowing seem more disordered. 
*/


import java.util.*;
import java.awt.*;


public class SnowManager
{ 
  private static final int NUM_FLAKES = 3000;   // total number of flakes
  private static final int START_BATCH = 20;   // number of flakes in a batch

  private Snowflake[] snow;
  private Random rand;



  public SnowManager(int pWidth, int pHeight, ViewerPanel vp)
  {
    rand = new Random();
    snow = new Snowflake[NUM_FLAKES];     // create all the snowflakes
    for(int i=0; i < NUM_FLAKES; i++)
      snow[i] = new Snowflake(rand, pWidth, pHeight, vp);
  }  // end of SnowManager()



  public void update()
  {
    startSomeFlakes();
    for(int i=0; i < NUM_FLAKES; i++)
      snow[i].update();
  }  // end of update()



  private void startSomeFlakes()
  /* Randomly select up to START_BATCH invisible flakes to make 
     visible at the top of the panel. They will start falling
     when updated. */
  {
    int numStarted = 0;
    int i = 0;
    while ((numStarted < START_BATCH) && (i < NUM_FLAKES)) {
      if (!snow[i].isVisible()) {     // if flake invisible
        if (rand.nextBoolean() == true) {   // maybe
           snow[i].setPosition(); 
             // position at top of panel (so becomes visible)
           numStarted++;
        }
      }
      i++;
    }
  }  // end of startSomeFlakes()



  public void draw(Graphics2D g2)
  { for(int i=0; i < NUM_FLAKES; i++)
      snow[i].draw(g2);
  }


}  // end of SnowManager class
