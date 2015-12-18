package m.breakout;
// Paddle.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* The paddle can move left and right along the
   bottom of the screen. It is controlled by the user's hand.

   When the user swipes their hand +/- SWIPE_RANGE around
   the midpoint line of the screen, this will make the
   paddle moves across the full width of the screen.

   A paddle can have a direction based on comparing its last known
   x-axis positionj with the current one. The three paddle direction
   values are defined in the PaddleDir enumeration.
*/

import java.awt.*;


enum PaddleDir {     // direction of paddle movement
    LEFT, RIGHT, NONE
}



public class Paddle extends Sprite
{
  private static final int SWIPE_RANGE = 200;
     /* full range is 2*SWIPE_RANGE, centered on the screen's midpoint
        line */

  
  private int xPrev = -1;     // previous paddle position

  private int mid, upperLimit, lowerLimit;
     // lower-to-upper limit is SWIPE_RANGE around the midpoint line (mid)


  public Paddle(Dimension scrDim) 
  { 
    super("carriage.png", scrDim);  

    setPosition( (getPWidth()-width)/2, getPHeight()-height); 
                     // positioned at the bottom of the panel, near the center
    setStep(0,0);  // no movement initially

    mid = getPWidth()/2;     // the midpoint of the panel

    // calculate lower-to-upper limits
    lowerLimit = mid - SWIPE_RANGE;
    if (lowerLimit < 0)
      lowerLimit = 0;

    upperLimit = mid + SWIPE_RANGE;
    if (upperLimit > getPWidth())
      upperLimit = getPWidth();
    // System.out.println("lower - mid - upper: " + lowerLimit + " - " + mid +
    //                            " - " + upperLimit);
  } // end of Paddle()



  public void moveTo(int x)
  // move the paddle to x-position after scaling
  {
    if ((x >= 0) && (x+width < getPWidth())) {
      xPrev = locx;
      locx = scaleX(x);
      // System.out.println("x - locx: " + x + " - " + locx);
    }
  }  // end of moveTo()


  private int scaleX(int x)
  /* The x value is the x-axis position of the hand on the full-size screen,
     which is truncated and scaled so that lower-to-upper is mapped to 0-100.
     This x position is then enlarged back up to full-screen size. */
  {
    // truncate lower-upper so xScale is in the range 0-100
    int xScale = 0;
    if ((lowerLimit < x) && (x < upperLimit))    
      xScale = ((x - mid) * 100/(2 * SWIPE_RANGE)) + 50;
    else if (x >= upperLimit)
      xScale = 100;

    return (xScale * (getPWidth()-width)/100);    
                    /* enlarge to full-screen range, but with paddle not
                       extending off the rhs of the screen. */
  }  // end of scaleX()

  
  public PaddleDir getDir()
  // return direction by comparing current location with previous one
  {
    if ((xPrev == -1) || (xPrev == locx))
      return PaddleDir.NONE;
    else if (xPrev < locx)
      return PaddleDir.RIGHT;
    else
      return PaddleDir.LEFT;
  }  // end of getDir()
  

}  // end of Paddle class
