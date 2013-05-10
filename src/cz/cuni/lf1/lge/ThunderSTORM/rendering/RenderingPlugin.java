package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class RenderingPlugin implements PlugIn {

  private static final String[] METHODS = new String[]{"Density", "ASH", "Histogram", "Scatter"};
  private static final String LABEL_X_POS = "x [px]";
  private static final String LABEL_Y_POS = "y [px]";

  @Override
  public void run(String string) {
    ResultsTable rt = Analyzer.getResultsTable();
    if (rt == null) {
      IJ.error("Requires Results window open");
      return;
    }
    if (rt.getColumnIndex(LABEL_X_POS) == ResultsTable.COLUMN_NOT_FOUND || rt.getColumnIndex(LABEL_Y_POS) == ResultsTable.COLUMN_NOT_FOUND) {
      IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X_POS, LABEL_Y_POS, rt.getColumnHeadings()));
      return;
    }

    double[] xpos = rt.getColumnAsDoubles(rt.getColumnIndex(LABEL_X_POS));
    double[] ypos = rt.getColumnAsDoubles(rt.getColumnIndex(LABEL_Y_POS));
    if (xpos != null && ypos != null) {
      IJ.error("results were null");
      return;
    }

    GenericDialog gd = new GenericDialog("New Image");
    gd.addChoice("Method", METHODS, "ASH");
    gd.addNumericField("Image_size_X", 0, 0);
    gd.addNumericField("Image_size_Y", 0, 0);
    gd.addNumericField("Resolution", 0.2, 3);

    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    }
    String selectedMethod = gd.getNextChoice();
    int imSizeX = (int) gd.getNextNumber();
    int imSizeY = (int) gd.getNextNumber();
    double resolution = gd.getNextNumber();

    if ("Density".equals(selectedMethod)) {
      
    } else if ("ASH".equals(selectedMethod)) {
    } else if ("Histogram".equals(selectedMethod)) {
    } else if ("Scatter".equals(selectedMethod)) {
    } else {
      IJ.error("Unknown rendering method.");
      return;
    }
  }
}
