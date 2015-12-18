package c.depthchart;


// SimpleXYSeries.java
// Andrew Davison, September 2012, ad@fivedots.coe.psu.ac.th

/* An XY bar chart created using JFreeChart 
  (http://www.jfree.org/jfreechart/)
*/


import java.awt.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;


public class SimpleXYSeries extends JFrame 
{

  public SimpleXYSeries() 
  {
    super("Simple XY Series");

    XYSeriesCollection dataset = createDataset();

    // put the data into a chart
    JFreeChart chart = ChartFactory.createXYBarChart(
                            "Simple XY Series", "Time (secs)", false, "Hz",    // is x a date axis?
                            dataset, PlotOrientation.VERTICAL,
                            true, true, false);    // legend?, tooltips?, urls?

    // add the chart to a panel
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 270));
    setContentPane(chartPanel);

    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }  // end of SimpleXYSeries()

  

  private XYSeriesCollection createDataset() 
  {
    // a sequence of (x,y) data items
    XYSeries series = new XYSeries("Frequency Burst Times");
    series.add(1.0, 400.2);
    series.add(5.0, 294.1);
    series.add(4.0, 100.0);
    series.add(12.5, 734.4);
    series.add(17.3, 453.2);
    series.add(21.2, 500.2);
    series.add(21.9, null);     // null means missing value
    series.add(25.6, 734.4);
    series.add(30.0, 453.2);

    XYSeriesCollection dataset = new XYSeriesCollection(series);
    return dataset;
  }  // end of createDataset()

  

  
  // ------------------------------------------------------------

  public static void main(String[] args) 
  {  new SimpleXYSeries();  } 

}  // end of SimpleXYSeries class
