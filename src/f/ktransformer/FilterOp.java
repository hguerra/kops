package f.ktransformer;


// FilterOp.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* An Enumeration for filters. Each filter has three parts -- its 
   enumeration constant, a name string, and a BufferedImageOp
   filter operation.

   Some of the filters are updateable (via update()), which change 
   some parameter of their filter op. This means that the next time
   the filter is applied (with apply()), the resulting image will
   be different.

   The current updateable filters: BOX_BLUR and DISSOLVE

   The filters come from:
        http://www.jhlabs.com/ip/filters/
   and were written by Jerry Huxtable.
*/

import java.awt.*;
import java.awt.image.*;

import com.jhlabs.image.*;



public enum FilterOp 
{
  // declare constants, name strings, and BufferedImage ops
  CHROME("Chrome", op("Chrome")), 
  MARBLE("Marble", op("Marble")), 
  CRYSTAL("Crystal", op("Crystal")), 
  BLOCK("Block", op("Block")),
  BOX_BLUR("Box Blur", op("Box Blur")), 
  RIPPLE("Ripple", op("Ripple")), 
  DISSOLVE("Dissolve", op("Dissolve"));

  private static final int CYCLE_TIME = 3000;   
             // the length of time (in ms) for an update cycle for a filter op

  private static int hideBGPixel = new Color(0, 0, 255, 0).getRGB();   // transparent blue

  // enum parameters
  private String name;
  private BufferedImageOp op;



  FilterOp(String name, BufferedImageOp op) 
  {  this.name = name; 
     this.op = op;
  }


  private static BufferedImageOp op(String name)
  /* initialize the filter operation for the given constant.
     All these ops come from:
       http://www.jhlabs.com/ip/filters/
  */
  {
    BufferedImageOp op = null;
    System.out.println("Creating op for " + name);

    if (name.equals("Chrome")) {
      op = new ChromeFilter();
      ((ChromeFilter)op).setBumpHeight(3f);
    }
    else if (name.equals("Marble")) {
      op = new MarbleFilter();
      ((MarbleFilter)op).setXScale(8f);
      ((MarbleFilter)op).setYScale(8f);
    }
    else if (name.equals("Crystal")) {
      op = new CrystallizeFilter();
      ((CrystallizeFilter)op).setEdgeColor(hideBGPixel);
    }
    else if (name.equals("Block")) {
      op = new BlockFilter();    
      ((BlockFilter)op).setBlockSize(10);
    }
    else if (name.equals("Box Blur"))      // can be updated later
      op = new BoxBlurFilter();
    else if (name.equals("Ripple")) {
      op = new RippleFilter();
     ((RippleFilter)op).setXAmplitude(12f);
    }
    else if (name.equals("Dissolve"))      // can be updated later
      op = new DissolveFilter(); 
    else
      System.out.println("Did not recognize op name");

    return op;
  }  // end of op()


  public String getName() 
  {  return name; }


  public static FilterOp fromName(String nm) 
  // convert a name string into its corresponding FilterOp enum
  {
    for (FilterOp fop : FilterOp.values())
      if (nm.equalsIgnoreCase(fop.name))
        return fop;
    return null;
  }  // end of fromName()




  public BufferedImage apply(BufferedImage im)
  // apply this enum's filter op to the supplied image
  {  
    if (op == null)
      return im;
    else
      return op.filter(im, null);  
  }  // end of apply()
  


  public void update(long totalTime)
  /* Change a paramter of the filter op for this enum.
     
    To keep things simple, the parameter change is based on the a
    cycle value which is a float that cycles between 0f and 1f.
    The cycle is calculated using the current totalTime value,
    but other techniques could be used.

    This means that the filter op change will also cycle.
  */
  { if (op == null)
      return;

    float cycle = (float)(totalTime % CYCLE_TIME)*2/CYCLE_TIME;    
              // produces a value between 0f and 2f
    if (cycle > 1f)
      cycle = 2f - cycle;     // so goes 0 - 1 - 0


    if (this == BOX_BLUR) {
      cycle = cycle*12f;    // so cycles 0 - 12 - 0
      ((BoxBlurFilter)op).setRadius((int)cycle);
    }
    else if (this == DISSOLVE)
      ((DissolveFilter)op).setDensity(cycle); 
  }  // end of update()


}  // end of FilterOp enum

