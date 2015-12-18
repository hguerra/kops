package m.breakout;
// Ball.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* A ball starts at the center of the screen and can bounce off the
   walls, ceilings, paddle, and bricks. When it hits a brick, the brick 
   disappears. If the ball goes below the floor level, then the user
   loses a life, and the ball is reinitialized by calling init() again.

   When the ball hits a paddle, if the paddle direction matches the 
   ball's direction, then the ball is increased by INCR. It can also
   slow down if the velocities are opposite.
*/

import java.awt.*;
import java.util.*;


public class Ball extends Sprite
{
  private static final int STEP = 15;  
  private static final int INCR = 10;    // for speeding up / slowing down the sprite

  private Random rand;  


  public Ball(Dimension scrDim) 
  { 
    super("tennis.png", scrDim); 
    rand = new Random();    // for left or right movement at start-time
    init(); 
  }  // end of Ball()


  public void init()
  {
    setPosition((getPWidth()-width)/2, getPHeight()/2);    // always start at screen center
    int xStep = (rand.nextBoolean()) ? STEP : -STEP;
    setStep(xStep, STEP);     // always move down, but may be to left or right
  }


  public boolean belowFloor()
  {
     if ((locy >= getPHeight()) && (dy > 0)) // off bottom and moving down
       return true;
     return false;
  }  // end of belowFloor()




  public void update(Paddle paddle, BricksManager bricksMan)
  {
    bounceOffWalls();

    // if the ball has hit the paddle, make it bounce up
    if (hasHit(paddle)) {
      dy = -dy; 
      locy += 5*dy;    // move away from paddle, so won't hit on next frame

      // adjust speed of ball based on paddle's direction of movement
      PaddleDir dir = paddle.getDir();
      if (dir == PaddleDir.LEFT)
        dx =- INCR;
      else if (dir == PaddleDir.RIGHT)
        dx += INCR;
    }

    // rebound off a brick that's been hit
    Brick brick = bricksMan.hitBy(this);
    if (brick != null) {
      reboundFrom(brick);
    }

    super.update();
  } // end of update()



  private void bounceOffWalls()
  // Respond when the ball hits a wall or ceiling.
  {
    if ((locy <= 0) && (dy < 0))   // touching top and moving up
      dy = -dy;

    if ((locx <= 0) && (dx < 0))    // touching lhs and moving left
      dx = -dx;   // move right
    else if ((locx+width >= getPWidth()) && (dx > 0))
		                           // touching rhs and moving right
      dx = -dx;   // move left
  }  // end of bounceOffWalls()




  private void reboundFrom(Brick brick)
  // make the ball bounce away from the brick
  { 
    Rectangle brickRect = brick.getRect();   // bounding box

    // calculated edge points of ball
    Point ptRight = new Point(locx + width+1, locy);
    Point ptLeft = new Point(locx-1, locy);
    Point ptTop = new Point(locx, locy-1);
    Point ptBottom = new Point(locx, locy + height+1);

    // test edge points against brick's bounded box
    if (brickRect.contains(ptRight) ||
        brickRect.contains(ptLeft) )
      dx = -dx;     // rebound from left or right edges

    if (brickRect.contains(ptTop)  ||
        brickRect.contains(ptBottom) )
      dy = -dy;     // rebound from top or bottom edges
  } // end of reboundFrom()


}  // end of Ball class
