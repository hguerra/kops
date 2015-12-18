package m.breakout;
// BreakoutPanel.java
// Andrew Davison, Nov 2011, ad@fivedots.coe.psu.ac.th

/* The game's drawing surface, which covers the entire screen.

   The run() methods shows the standard coding style. The thread
   enters a  4-element cycle:
      wait for Kinect input / update game elements / draw game / sleep
   The sleep time tries to keep each cycle close to CYCLE_TIME ms.

   The game is paused until the user performs a "click" operation.
   After that, hand movements cause the paddle to swing left and right.

   The game can be paused, either by typing 'p', or by the user hiding
   their hands. The game is resumed with another 'p' or by the user
   carrying out a hand "click".

   The game ends when:
     * the user presses ESC, q, ctrl-c;

     * the user hides their hands long enough;

     * the user deletes all the bricks;

     * all the user's lives are lost (he starts with MAX_LIVES, and loses
       one each time the ball drops below the floir.)

   The Kinect hand interface is based on code from
   TrackersPanel in the HandsTracker application
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

import java.nio.ByteBuffer;

import org.OpenNI.*;
import com.primesense.NITE.*;


enum SessionState {
    IN_SESSION, NOT_IN_SESSION, QUICK_REFOCUS
}


public class BreakoutPanel extends JPanel implements Runnable
{
  private static final int MAX_LIVES = 10; 
  private static final int CYCLE_TIME = 25;  // time for one game iteration, in ms 


  private boolean isRunning = false;   // used to stop the game loop
  private boolean isPaused = true;     // pauses the game
  private boolean gameOver = false;    // has the user finished the game?

  private int pWidth, pHeight;         // panel size

  // for game messages
  private Font font;
  private String gameStatusMsg = null;

  // game elements
  private Ball ball;
  private Paddle paddle;
  private BricksManager bricksMan;
  private int livesLeft = MAX_LIVES;

  // Kinect camera specific
  private BufferedImage camImage = null;
  private double scaleFactor = 1.0;
  private int scaledWidth, scaledHeight;
  private int mapWidth, mapHeight;

  // OpenNI and NITE vars
  private Context context;
  private ImageGenerator imageGen;
  private DepthGenerator depthGen;

  private SessionManager sessionMan;
  private SessionState sessionState;



  public BreakoutPanel()
  {
    // set panel/game dimensions to be those of the entire screen
    Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();   // screen size
    setPreferredSize(scrDim); 
    pWidth = scrDim.width;
    pHeight = scrDim.height;
    System.out.println("Panel (w,h) : (" + pWidth + ", " + pHeight + ")");

    // set up message font
    font = new Font("SansSerif", Font.BOLD, 36);

    // listen for key presses
    setFocusable(true);
    requestFocus();    // the JPanel now has focus, so receives key events
    initKeyListener();

    // create game elements
    ball = new Ball(scrDim);
    paddle = new Paddle(scrDim);
    bricksMan = new BricksManager(scrDim);
    gameStatusMsg = "Lives left: " + livesLeft;

    configKinect();

    new Thread(this).start();   // start updating the panel
  }  // end of BreakoutPanel()



  private void initKeyListener()
  // define keys for stopping, pausing, resuming the game
  {
    addKeyListener( new KeyAdapter() {
       public void keyPressed(KeyEvent e)
       { 
         int keyCode = e.getKeyCode();
         if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) ||
             ((keyCode == KeyEvent.VK_C) && e.isControlDown()) )
                // ESC, q, ctrl-c to stop isRunning 
           isRunning = false;
         else if (keyCode == KeyEvent.VK_P)  // toggle pausing/resume with 'p'
           isPaused = !isPaused;
       }
     });
  }  // end of initKeyListener()



  // -------------- OpenNI / NITE listeners ----------------



  private void configKinect()
  // set up OpenNI and NITE generators and listerners
  {
    try {
      context = new Context();

      // add the NITE Licence
      License licence = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
      context.addLicense(licence);

      // set up image and depth generators
      imageGen = ImageGenerator.create(context);
           // for displaying the scene
      depthGen = DepthGenerator.create(context);
           // for converting real-world coords to screen coords

      MapOutputMode mapMode = new MapOutputMode(640, 480, 30);   // xRes, yRes, FPS
      imageGen.setMapOutputMode(mapMode);
      depthGen.setMapOutputMode(mapMode);

      imageGen.setPixelFormat(PixelFormat.RGB24);

      ImageMetaData imageMD = imageGen.getMetaData();
      mapWidth = imageMD.getFullXRes();
      mapHeight = imageMD.getFullYRes();
      System.out.println("Image dimensions (" + mapWidth + ", " + mapHeight + ")");

      // calculate scaling for Kinect image and hand coords
      scaleFactor = ((double)pWidth)/mapWidth;
      System.out.println("Image scale factor: " + scaleFactor);
      scaledWidth = pWidth;
      scaledHeight = (int) (mapHeight * scaleFactor);
      System.out.println("Scaled (w,h): (" + pWidth + ", " + scaledHeight + ")");

      // set Mirror mode for all
      context.setGlobalMirror(true);

      // set up hands and gesture generators
      HandsGenerator hands = HandsGenerator.create(context);
      hands.SetSmoothing(0.1f);

      GestureGenerator gesture = GestureGenerator.create(context);

      context.startGeneratingAll();
      System.out.println("Started context generating...");

      // set up session manager and points listener
      sessionMan = new SessionManager(context, "Click,Wave", "RaiseHand");
      setSessionEvents(sessionMan);
      sessionState = SessionState.NOT_IN_SESSION;

      // increase timeout from 15 secs to 60 secs (15000 --> 60000 ms)
      // System.out.println("Session refocus timeout: " + 
      //                       sessionMan.getQuickRefocusTimeout() );
      sessionMan.setQuickRefocusTimeout(60000);
      System.out.println("New Session refocus timeout: " + 
                             sessionMan.getQuickRefocusTimeout() );

      PointControl pointCtrl = initPointControl();
      sessionMan.addListener(pointCtrl);
    }
    catch (GeneralException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }  // end of configKinect()



  private void setSessionEvents(SessionManager sessionMan)
  // create session callbacks
  {
    try {
      // session start (S1)
      sessionMan.getSessionStartEvent().addObserver( new IObserver<PointEventArgs>() {
        public void update(IObservable<PointEventArgs> observable, PointEventArgs args)
        { System.out.println("Session started...");
          sessionState = SessionState.IN_SESSION;
        }
      });

      // session end (S2)
      sessionMan.getSessionEndEvent().addObserver( new IObserver<NullEventArgs>() {
        public void update(IObservable<NullEventArgs> observable, NullEventArgs args)
        { System.out.println("Session ended");
          isRunning = false;     // stop the game cycle
          sessionState = SessionState.NOT_IN_SESSION;
        }
      });
    }
    catch (StatusException e) {
      e.printStackTrace();
    }
  }  // end of setSessionEvents()




  private PointControl initPointControl()
  // create 4 hand point listeners
  {
    PointControl pointCtrl = null;
    try {
      pointCtrl = new PointControl();

      // create new hand point, and move paddle (P1)
      pointCtrl.getPointCreateEvent().addObserver( new IObserver<HandEventArgs>() {
        public void update(IObservable<HandEventArgs> observable, HandEventArgs args)
        { isPaused = false;    // start/resume the game
          movePaddle(args.getHand());     // allow user hands to move the paddle
        }
      });

      // hand point has moved; move paddle (P2)
      pointCtrl.getPointUpdateEvent().addObserver( new IObserver<HandEventArgs>() {
        public void update(IObservable<HandEventArgs> observable, HandEventArgs args)
        {  movePaddle(args.getHand());  }
      });

      // no active hand point, which triggers refocusing (P3, was P4)
      pointCtrl.getNoPointsEvent().addObserver( new IObserver<NullEventArgs>() {
        public void update(IObservable<NullEventArgs> observable, NullEventArgs args)
        {
          if (sessionState != SessionState.NOT_IN_SESSION) {
            System.out.println("  Lost hand point, so refocusing");
            sessionState = SessionState.QUICK_REFOCUS;
            isPaused = true;    // pause the game while refocusing
            // if refocusing takes too long then the session will automatically end
          }
        }
      });

    }
    catch (GeneralException e) {
      e.printStackTrace();
    }
    return pointCtrl;
  }  // end of initPointControl()



  private void movePaddle(HandPointContext handContext)
  {
    sessionState = SessionState.IN_SESSION;
    int x = getScrX( handContext.getPosition() );
    // System.out.println("  Hand at " + x);
    paddle.moveTo(x);
  }  // end of movePaddle()



  private int getScrX(Point3D realPt)
  /* convert real-world hand coordinate to Kinect camera image 
     dimensions (640x480), and then scale the x-value to 
    full-screen size. 
  */
  {
    try {
      Point3D pt = depthGen.convertRealWorldToProjective(realPt);
            // convert real-world coordinates to Kinect camera image dimensions
      if (pt != null)
        return (int)(pt.getX() * scaleFactor);    // scale x-coord to full-screen size
    }
    catch (StatusException e) 
    {  System.out.println("Problem converting point"); }
    return 0;
  }  // end of getScrX()


  // -------------------------------------------


  public void run()
  /* Implements the game loop, which has four main parts:
         wait for Kinect / update game / draw game / sleep

     The sleep time tries to keep each cycle close to CYCLE_TIME ms.
  */
  {
    long duration;
    isRunning = true;

    while(isRunning) {
      try {    // wait for Kinect input
        context.waitAnyUpdateAll();
        sessionMan.update(context);
      }
      catch(StatusException e)
      {  System.out.println(e);
         System.exit(1);
      }

      long startTime = System.currentTimeMillis();
      updateCameraImage();    

      // System.out.println("isPaused: " + isPaused + "; gameOver: " + gameOver);
      if (!isPaused && !gameOver)
        updateGame();   // update game elements

      duration = System.currentTimeMillis() - startTime;
      // System.out.println(" update duration: " + duration);

      repaint();

      if (duration < CYCLE_TIME) {
        try {
          Thread.sleep(CYCLE_TIME-duration);  // wait until CYCLE_TIME time has passed
        } 
        catch (Exception ex) {}
      }
    }

    // close down Kinect
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    System.exit(0);
  }  // end of run()



  public void closeDown() 
  {  isRunning = false; } 



  // ------------------- Kinect camera reading ------------------


  private void updateCameraImage()
  // update Kinect camera's image
  {
    try {
      ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
      camImage = bufToImage(imageBB);
    }
    catch (GeneralException e) {
      System.out.println(e);
    }
  }  // end of updateCameraImage()


  private BufferedImage bufToImage(ByteBuffer pixelsRGB)
  /* Transform the ByteBuffer of pixel data into a BufferedImage
     Converts RGB bytes to ARGB ints with no transparency.
  */
  {
    int[] pixelInts = new int[mapWidth * mapHeight];

    int rowStart = 0;
        // rowStart will index the first byte (red) in each row;
        // starts with first row, and moves down

    int bbIdx;               // index into ByteBuffer
    int i = 0;               // index into pixels int[]
    int rowLen = mapWidth * 3;    // number of bytes in each row
    for (int row = 0; row < mapHeight; row++) {
      bbIdx = rowStart;
      // System.out.println("bbIdx: " + bbIdx);
      for (int col = 0; col < mapWidth; col++) {
        int pixR = pixelsRGB.get( bbIdx++ );
        int pixG = pixelsRGB.get( bbIdx++ );
        int pixB = pixelsRGB.get( bbIdx++ );
        pixelInts[i++] =
           0xFF000000 | ((pixR & 0xFF) << 16) |
           ((pixG & 0xFF) << 8) | (pixB & 0xFF);
      }
      rowStart += rowLen;   // move to next row
    }

    // create a BufferedImage from the pixel data
    BufferedImage im =
       new BufferedImage( mapWidth, mapHeight, BufferedImage.TYPE_INT_ARGB);
    im.setRGB( 0, 0, mapWidth, mapHeight, pixelInts, 0, mapWidth );
    return im;
  }  // end of bufToImage()




  // --------------------- updating the game ---------------------


  private void updateGame()
  /* update the ball and paddle states;
     a brick may also become invisible while being 
     tested by ball.update()    */
  {  
    gameOver = isAtGameEnd();
    if (!gameOver) {
      ball.update(paddle, bricksMan);
      paddle.update();
    }
    else
      ball.setActive(false);   // make ball invisible
  }  // end of updateGame()


  private boolean isAtGameEnd()
  {
    if (ball.belowFloor()) {     // means the user has lost a life
      livesLeft--;
      if (livesLeft == 0) {
        gameStatusMsg = "All lives lost";
        return true;    // game is over
      }
      else {
        gameStatusMsg = "Lives left: " + livesLeft;
        ball.init();     // reposition the ball back on the screen
      }
    }

    if (bricksMan.numRemaining() == 0) {     // all the bricks have been deleted
      gameStatusMsg = "Victory!";
      return true;
    }

    return false;
  }  // end of isAtGameEnd()



  // --------------------- rendering ------------------------------


  public void paintComponent(Graphics g)
  /*  Draw in order:
         * the enlarged Kinect camera image;
         * the game elements: ball, paddle, and all the active bricks;
         * Kinect session state info;
         * game status info;
         * a special game over message (if the game is over)
  */
  {
    super.paintComponent(g);

    // fill the background with black
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, pWidth, pHeight);

    // draw scaled camera image as background
    if (camImage != null)
      g.drawImage(camImage, 0, 0, scaledWidth, scaledHeight, null);

    // draw game elements
    ball.draw(g);
    paddle.draw(g);
    bricksMan.draw(g);

    // draw status messages
    g.setFont(font);
    g.setColor(Color.YELLOW);

    drawSessionInfo(g);
    if (gameStatusMsg != null)
      g.drawString(gameStatusMsg, 10, pHeight-10);

    if (gameOver)
      showGameOver(g);
  } // end of paintComponent()



  private void drawSessionInfo(Graphics g)
  /* draw session state information to help the user start/resume
     a game with the right hand moves */
  {
    String msg = null;
    switch (sessionState) {
      case IN_SESSION:
        msg = "Tracking...";
        break;
      case NOT_IN_SESSION:
        msg = "Click/Wave to start tracking";
        break;
      case QUICK_REFOCUS:
        msg = "Click/Wave/Raise your hand to resume tracking";
        break;
    }
    if (msg != null)
      g.drawString(msg, 10, 35);  // top left
  }  // end of drawSessionInfo()



  private void showGameOver(Graphics g)
  // center the game-over message in the panel
  {
    String overMsg = "Game Over";
    Font overFont = new Font("SansSerif", Font.BOLD, 144);
    g.setFont(overFont);
    g.setColor(Color.RED);

    FontMetrics metrics = this.getFontMetrics(overFont);
    int x = (pWidth - metrics.stringWidth(overMsg))/2; 
    int y = (pHeight - metrics.getHeight())*2/3;
    g.drawString(overMsg, x, y);
  }  // end of showGameOver()



}  // end of BreakoutPanel class
