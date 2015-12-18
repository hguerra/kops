package m.breakout;
// Breakout.java
// Andrew Davison, December 2011, ad@fivedots.coe.psu.ac.th

/* A version of the classic Breakout game 
   (http://en.wikipedia.org/wiki/Breakout_(video_game))
   played using hand movements detected by the Kinect sensor.

   Use "click" to start/resume, wave a hand to move the paddle.

   Hide your hand to pause the game (too long a pause triggers 
   game termination).

   Type ESC, q, ctrl-c to exit the game.

   You win if you remove all the bricks. You lose if your 'number of
   lives' drops to 0.

   The game is full-screen, and hides the cursor.
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;


public class Breakout extends JFrame
{
  private BreakoutPanel bp;        // where the game is drawn

  public Breakout()
  {
    super();

    Container c = getContentPane(); 
    bp = new BreakoutPanel();
    c.add(bp, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { bp.closeDown();  }    // not likely to be used, since frame is undecorated
    });

    hideCursor(c);
    setUndecorated(true);
    pack();    // the panel makes itself full-screen size
    setResizable(false);
    setVisible(true);
  }  // end of Breakout()


  private void hideCursor(Container c)
  {
    // create a transparent 16 x 16 pixel cursor image
    BufferedImage cursorIm = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    // create a new blank cursor
    Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                                    cursorIm, new Point(0, 0), "blank cursor");

    // assign the blank cursor to the JFrame
    c.setCursor(blankCursor);
  }  // end of hideCursor()



   // ---------------------------------------------------

   public static void main(String args[])
   {  new Breakout(); }

} // end of Breakout

