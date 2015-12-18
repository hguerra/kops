package m.breakout;
// Sprite.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* A Sprite has a position, velocity (in terms of steps),
   an image used for rendering, and can be deactivated (invisible).
   It knows the domensions of the panel is which it is 
   being displayed.

   The image is assumed to be in the subdirectory IM_DIR. If the
   specified image cannot be loaded, then the sprite is drawn
   as a yellow circle.

   The update() and draw() methods update the spites position and redraw
   it respectively. A sprite is updated and drawn only when it is active.


   The hasHit() and getRect() methods are for collision detection between
   sprites.

   Sprite is subclassed by Brick, Paddle, and Ball in the Breakout example.
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;


public class Sprite 
{
  // default step sizes (how far to move in each update)
  private static final int STEP = 3; 

  // default dimensions when there is no image
  private static final int SIZE = 8;   


  private static final String IM_DIR = "images/";


  private Image image =  null;
  private int pWidth, pHeight;   // panel dimensions
  private boolean isActive = true;      
         // a sprite is updated and drawn only when it is active

  // protected vars
  protected int locx, locy;        // location of sprite
  protected int dx, dy;            // step to move in each update
  protected int width, height;     // sprite dimensions


  public Sprite(String fnm, Dimension scrDim) 
  { 
    pWidth = scrDim.width;
    pHeight = scrDim.height;
    // System.out.println("panel (w,h): (" + pWidth + ", "  + pHeight + ")");

    loadImage(fnm);    // load image and set width/height sprite dimensions 

    // start in center of panel by default
    locx = (pWidth-width)/2; 
    locy = (pHeight-height)/2;

    dx = STEP; dy = STEP;
  } // end of Sprite()



  private void loadImage(String fnm)
  // load image and set width/height sprite dimensions
  {
    image = new ImageIcon(IM_DIR + fnm).getImage();
    if (image != null) {
      // System.out.println("Loaded " + fnm);
      width = image.getWidth(null);
      height = image.getHeight(null);
    }
    else {
      System.out.println("Could not load image from " + fnm);
      width = SIZE;  height = SIZE;
    }
  }  // end of loadImage()


  public int getWidth()
  {  return width;  }

  public int getHeight()
  {  return height;  }


  public int getPWidth()   // of the enclosing panel
  {  return pWidth;  }

  public int getPHeight()  // of the enclosing panel
  {  return pHeight;  }


  public boolean isActive() 
  {  return isActive;  }

  public void setActive(boolean a) 
  {  isActive = a;  }


  public void setPosition(int x, int y)
  {  locx = x; locy = y;  }

  public int getX()
  {  return locx;  }

  public int getY()
  {  return locy;  }


  public void setStep(int x, int y)
  {  dx = x; dy = y;  }


  public boolean hasHit(Sprite sprite)
  /* this sprite has hit the other one if their 
     bounding rectangles intersect  */
  {
    Rectangle thisRect = getRect();
    Rectangle spriteRect = sprite.getRect();

    if (thisRect.intersects(spriteRect))
      return true;
    return false;
  }  // end of hasHit()



  public Rectangle getRect()
  {  return  new Rectangle(locx, locy, width, height);  }


  public void update()
  {
    if (isActive()) {
      locx += dx;
      locy += dy;
    }
  } // end of update()



  public void draw(Graphics g) 
  {
    if (isActive()) {
      if (image == null) {   // the sprite has no image
        g.setColor(Color.YELLOW);   // draw a yellow circle instead
        g.fillOval(locx, locy, SIZE, SIZE);
        g.setColor(Color.BLACK);
      }
      else
        g.drawImage(image, locx, locy, null);
    }
  } // end of draw()


}  // end of Sprite class
