package c.depthchart;

// SimplePie.java
// Andrew Davison, September 2012, ad@fivedots.coe.psu.ac.th

/* A 3D pie chart created using JFreeChart 
  (http://www.jfree.org/jfreechart/)
*/


import java.awt.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;


public class SimplePie extends JFrame 
{

  public SimplePie() 
  {
    super("Simple Pie Chart");

    // create the data set
    DefaultPieDataset dataset = new DefaultPieDataset();
    dataset.setValue("John", 30);
    dataset.setValue("Andrew", 30);
    dataset.setValue("Supatra",40);

    // put the data into a chart
    JFreeChart chart = ChartFactory.createPieChart3D(
                                     "Family Pie Chart",   // title
                                      dataset, true, true, false);    // legend?, tooltips?, urls?

    // add the chart to a panel
    ChartPanel chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 270));
    setContentPane(chartPanel);

    pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }  // end of SimplePie()

  
  // ------------------------------------------------------------

  public static void main(String[] args) 
  {  new SimplePie();  } 

}  // end of SimplePie class
