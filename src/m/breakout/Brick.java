package m.breakout;
// Brick.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* A brick is an unmoving sprite, which will be deactivated when hit by 
   a ball.
*/


import java.awt.*;


public class Brick extends Sprite
{

  public Brick(String fnm, int x, int y, Dimension scrDim) 
  { 
    super(fnm, scrDim);  

    setPosition(x, y);
    setStep(0, 0);    // not moving
  }  // end of Brick()


}  // end of Brick class

