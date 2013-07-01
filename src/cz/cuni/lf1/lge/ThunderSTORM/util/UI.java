package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.IJ;
import javax.swing.UIManager;

public class UI {
  
  public static void setLookAndFeel() {
    // Use an appropriate Look and Feel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      //UIManager.put("swing.boldMetal", Boolean.FALSE);
    } catch (Exception ex) {
      IJ.handleException(ex);
    }
  }

}
