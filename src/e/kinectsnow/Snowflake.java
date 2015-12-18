package e.kinectsnow;
// Snowflake.java
// Andrew Davison, April 2012, ad@fivedots.coe.psu.ac.th

/* Represents a single snowflake.
   It falls from a random position at the top edge of the panel,
   and either disappears off the bottom edge or stops when it reaches
   the top-edge of a user.

*/

import java.util.*;
import java.awt.*;


public class Snowflake
{
  private static final int Y_DROP = 10;     // max down step
  private static final int X_DRIFT = 6;     // max side-to-side step

  private static final int SIZE = 6;   // of the flake

  private int pWidth, pHeight;   // of panel
  private Random rand;
  private int x, y;   // position of flake

  private ViewerPanel vp;


  public Snowflake(Random r, int w, int h, ViewerPanel vp)
  {
    rand = r;
    pWidth = w;  pHeight = h;
    this.vp = vp;

    y = -1;  // not visible
  }  // end of Snowflake()


  public boolean isVisible()
  // a flake is visible if within the panel's boundaries
  {  
    if ((x < 0) || (x >= pWidth) ||
        (y < 0) || (y >= pHeight)) 
      return false;
    return true;
  }  // end of isVisible()

 
  public void setPosition()
  {  
    x = rand.nextInt(pWidth);
    y = rand.nextInt(Y_DROP);  
  }


  public void update()
  /* A flake will tend to drift downwards unless it finds itself on 
     the top-edge of a user which causes it to stop. 

     If the user moves this top edge condition will no longer be true
     and the flake will resume falling.
  */
  {
    if (isVisible()) {
      if (isOnTopEdgeOfUser()) 
        return;   // do not update (i.e. stop moving)
      else { 
        x += rand.nextInt(X_DRIFT) - X_DRIFT/2;    // small sideways movement
        y += rand.nextInt(Y_DROP);                 // move down
      }
    }
  }  // end of update()


  private boolean isOnTopEdgeOfUser()
  // test if the flake is on the top edge of a user
  {
    if (vp.onUser(x,y) && !vp.onUser(x,y-SIZE*2))
       return true;
    return false;
  }  // end of isOnTopEdgeOfUser()



  public void draw(Graphics2D g2)
  // the flake is a small white circle
  {
    if (isVisible()) {
	  g2.setColor(Color.WHITE);
	  g2.fillOval(x-SIZE/2, y-SIZE/2, SIZE, SIZE);
    }
  }  // end of draw()


}  // end of Snowflake class
