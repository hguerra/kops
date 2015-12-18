package c.depthchart;


// ViewerPanel.java
// Andrew Davison, September 2012, ad@fivedots.psu.ac.th
// Version 2; copy to parent directory to use with OpenNIViewer.java

/* Based on OpenNI's SimpleViewer example
     Initialize OpenNI *without* using an XML file;
     Display a grayscale depth map (darker means further away, although black
     means "too close" for a depth value to be calculated).

   Added a JFreeChart XY bar chart to show counts for different depths.
*/

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.DecimalFormat;
import java.io.*;
import javax.imageio.*;
import java.util.*;

import org.OpenNI.*;

import java.nio.ShortBuffer;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.data.general.*;
import org.jfree.data.xy.*;



public class ViewerPanel extends JPanel implements Runnable
{
  private static final int MAX_DEPTH_SIZE = 10000;  

  private static final int CHART_MAX_DEPTH = 3700;     // used for chart x-axis
  private static final int CHART_DELAY =  2000;         // time between redraws

  // image vars
  private byte[] imgbytes;
  private BufferedImage image = null;   // for displaying the depth image
  private int imWidth, imHeight;
  private float histogram[];       // for the depth values
  private int maxDepth = 0;      // largest depth value

  private volatile boolean isRunning;
  
  // used for the average ms processing information
  private int imageCount = 0;
  private long totalTime = 0;
  private DecimalFormat df;
  private Font msgFont;

  // OpenNI
  private Context context;
  private DepthMetaData depthMD;

  // JFreeChart
  private XYSeries series;
  private int chartTime = CHART_DELAY;   
                  // so no delay before first update


  public ViewerPanel()
  {
    setBackground(Color.WHITE);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    initChart();
    configOpenNI();

    histogram = new float[MAX_DEPTH_SIZE];

    imWidth = depthMD.getFullXRes();
    imHeight = depthMD.getFullYRes();
    System.out.println("Image dimensions (" + imWidth + ", " +
                                              imHeight + ")");

    // create empty image object of correct size and type
    imgbytes = new byte[imWidth * imHeight];
    image = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_BYTE_GRAY);

    new Thread(this).start();   // start updating the panel's image
  } // end of ViewerPanel()


  private void initChart()
  // create the dataset, chart, panel, and window
  { 
    // create an empty data set
    series = new XYSeries("Depth Counts Histogram");
    for (int i = 0; i <= CHART_MAX_DEPTH; i++)
      series.add(i,0);     // depth with a zero count
    XYSeriesCollection dataset = new XYSeriesCollection(series);

    // put the data into a chart
    JFreeChart chart = ChartFactory.createXYBarChart(
                 "Depth Histogram", "Depth (mm)", false, "Depth Count", 
                 dataset, PlotOrientation.VERTICAL,
                 false, true, false );    // legend, tooltips, urls

    // modify the chart axes
    XYPlot plot = (XYPlot) chart.getPlot();

    NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();    // x-axis
    domainAxis.setVerticalTickLabels(true);
    domainAxis.setRange(0, CHART_MAX_DEPTH);
    domainAxis.setTickUnit(new NumberTickUnit(100));

    ValueAxis rangeAxis = plot.getRangeAxis();    // y-axis
    rangeAxis.setRange(0,15000);  // a bit of a guess

    // add the chart to a panel
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(1000, 500));

    // add the panel to a window
    JFrame chartFrame = new JFrame("Depth Histogram");
    chartFrame.setContentPane(chartPanel);
    chartFrame.pack();
    chartFrame.setVisible(true);
  }  // end of initChart()



  private void configOpenNI()
  // create context and depth generator
  {
    try {
      context = new Context();
      
      // add the NITE Licence 
      License licence = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");   // vendor, key
      context.addLicense(licence); 
      
      DepthGenerator depthGen = DepthGenerator.create(context);
      MapOutputMode mapMode = new MapOutputMode(640, 480, 30);   // xRes, yRes, FPS
      depthGen.setMapOutputMode(mapMode); 
      
      // set Mirror mode for all 
      context.setGlobalMirror(true);

      context.startGeneratingAll(); 
      System.out.println("Started context generating..."); 

      depthMD = depthGen.getMetaData();   
           // use depth metadata to access depth info (avoids bug with DepthGenerator)
    } 
    catch (Exception e) {
      System.out.println(e);
      System.exit(1);
    }
  }  // end of configOpenNI()



  public Dimension getPreferredSize()
  { return new Dimension(imWidth, imHeight); }


  public void run()
  /* update and display the depth image whenever the context
     is updated.
  */
  {
    isRunning = true;
    while (isRunning) {
      try {
        context.waitAnyUpdateAll();
      }
      catch(StatusException e)
      {  System.out.println(e); 
         System.exit(1);
      }
      long startTime = System.currentTimeMillis();
      updateDepthImage();
      imageCount++;
      totalTime += (System.currentTimeMillis() - startTime);
      chartTime += (System.currentTimeMillis() - startTime);
      repaint();
    }

    // close down
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    System.exit(1);
  }  // end of run()


  public void closeDown()
  {  isRunning = false;  } 



  private void updateDepthImage()
  /* build a new histogram of 8-bit depth values, and convert it to
     image pixels (as bytes) */
  {
    ShortBuffer depthBuf = depthMD.getData().createShortBuffer();
    calcHistogram(depthBuf);
    depthBuf.rewind();

    while (depthBuf.remaining() > 0) {
      int pos = depthBuf.position();
      short depth = depthBuf.get();
      imgbytes[pos] = (byte) histogram[depth];
    }
  }  // end of updateDepthImage()



  private void calcHistogram(ShortBuffer depthBuf)
  {
    // reset histogram[]
    for (int i = 0; i <= maxDepth; i++)
      histogram[i] = 0;

    // record number of different depths in histogram[]
    int numPoints = 0;
    maxDepth = 0;
    while (depthBuf.remaining() > 0) {
      short depthVal = depthBuf.get();
      if (depthVal > maxDepth)
        maxDepth = depthVal;
      if ((depthVal != 0)  && (depthVal < MAX_DEPTH_SIZE)){    // skip histogram[0]
        histogram[depthVal]++;
        numPoints++;
      }
    }
    // System.out.println("No. of numPoints: " + numPoints);
    // System.out.println("Maximum depth: " + maxDepth);

    if (chartTime > CHART_DELAY) {
      updateChart(histogram, maxDepth);
      chartTime = 0;
    }

    // convert into a cummulative depth count (skipping histogram[0])
    for (int i = 1; i <= maxDepth; i++)
      histogram[i] += histogram[i-1];

    /* convert cummulative depth into 8-bit range (0-255), which will later become grayscales
        - darker means further away, although black
          means "too close" for a depth value to be calculated).
    */
    if (numPoints > 0) {
      for (int i = 1; i <= maxDepth; i++)   // skipping histogram[0]
        histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) numPoints)));
    }
  }  // end of calcHistogram()



  private void updateChart(float histogram[], int maxDepth)
  /* The version of histogram[] used here contains a count of depths such that
    histogram[i] represents millimeter depth i. The array is turned into a 
    cummulative depth count after this method has processed the data.

    The calls to XYSeries.update() will automatically cause JFreeChart to redraw
    the chart panel.
  */
  {
    if (maxDepth > CHART_MAX_DEPTH) 
      System.out.println("Maximum depth (" + maxDepth + ") exceeds chart max depth");
    for (int i = 1; i <= CHART_MAX_DEPTH; i++) {      
      // skipping histogram[0] which is for unknown depths
      try {
        series.update(((Number)Integer.valueOf(i)), histogram[i]);
      }
      catch(SeriesException e) {
        System.out.println("Problem updating (" + i + ", " + histogram[i] + ")");
      }
    }
    // chartPanel.repaint();     // no need to explicitly trigger a repaint
  }  // end of updateChart()



  public void paintComponent(Graphics g)
  // Draw the depth image and statistics info
  { 
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    // convert image pixel array into an image
    DataBufferByte dataBuffer = new DataBufferByte(imgbytes, imWidth * imHeight);
    Raster raster = Raster.createPackedRaster(dataBuffer, imWidth, imHeight, 8, null);
    image.setData(raster);
    if (image != null)
      g2.drawImage(image, 0, 0, this);

    writeStats(g2);
  } // end of paintComponent()

  

  private void writeStats(Graphics2D g2)
  /* write statistics in bottom-left corner, or
     "Loading" at start time */
  {
    g2.setColor(Color.BLUE);
    g2.setFont(msgFont);
    int panelHeight = getHeight();
    if (imageCount > 0) {
      double avgGrabTime = (double) totalTime / imageCount;
      g2.drawString("Pic " + imageCount + "  " +
                   df.format(avgGrabTime) + " ms", 
                   5, panelHeight-10);  // bottom left
    }
    else  // no image yet
      g2.drawString("Loading...", 5, panelHeight-10);
  }  // end of writeStats()



} // end of ViewerPanel class

