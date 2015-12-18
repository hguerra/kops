package f.ktransformer;
// KTransformer.java
// Andrew Davison, April 2012, ad@fivedots.psu.ac.th

/* The Kinect RGB image is modified so that the user (or users)
   are shown against a background image of a park.
   The background changing code is based on the ChangeBG example.

   The application applies an image filter op to the camera image
   to change its appearance. The current filter can be 
   changed by the user selecting a new op from a menu. 

   The filters are defined as an enumeration called FilterOp, and
   are selected from a menu. The filters operations come from:
        http://www.jhlabs.com/ip/filters/
   and were written by Jerry Huxtable.

   Two of the filters ("Box Blur" and "Dissolve") cycle through a series
   of changes.

   Usage:
      > run KTransformer
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
// import java.util.*;



public class KTransformer extends JFrame 
                        implements ActionListener

{
  private static final FilterOp START_FOP = FilterOp.MARBLE;

  private ViewerPanel viewerPanel;


  public KTransformer()
  {
    super("Kinect Transformer");

    buildFilterMenu(START_FOP);
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    viewerPanel = new ViewerPanel(START_FOP);
    c.add( viewerPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { viewerPanel.closeDown(); }   // stop showing images
    });

    setResizable(false);
    pack();  
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of KTransformer()



  private void buildFilterMenu(FilterOp startFop)
  /* Construct a menu bar with a dingle "Filters" menu.
     The menu items are created from the filter name strings in
     the FilterOp enumeration.

     The menu items are grouped radio buttons with the startFop 
     filter initially on, and the others off.
  */
  {
    JMenuBar mb = new JMenuBar();
    JMenu fMenu = new JMenu("Filters");
    mb.add(fMenu);

    // build the menu items
    FilterOp[] values = FilterOp.values();  // get all FilterOp constants
    JRadioButtonMenuItem mi;
    ButtonGroup group = new ButtonGroup();
    for(FilterOp fop : values) {
      //  System.out.println(fop.getName());
      mi = new JRadioButtonMenuItem(fop.getName());   // create menu item from FilterOp name
      if (fop == startFop)
        mi.setSelected(true);   // set startFop menu item to be "on"
      mi.addActionListener(this);
      fMenu.add(mi);
      group.add(mi);
    }

    setJMenuBar(mb);
  }  // end of buildFilterMenu()


  public void actionPerformed(ActionEvent e)
  /* triggered by a user's menu selection.
     get the selected menu item's name, look up its associated
     FilterOp constant, and pass it to the panel to
     change the current filter.
  */
  {    
    JRadioButtonMenuItem mi =(JRadioButtonMenuItem)e.getSource();
    mi.setSelected(true);
    String filterName = mi.getText();   // get selected item's name
    System.out.println("Selected: " + filterName);
    FilterOp fop = FilterOp.fromName(filterName);   // name --> FilterOp
    viewerPanel.setFilter(fop);
  }  // end of actionPerformed()



  // -------------------------------------------------------

  public static void main( String args[] )
  { new KTransformer(); }  

} // end of KTransformer class
