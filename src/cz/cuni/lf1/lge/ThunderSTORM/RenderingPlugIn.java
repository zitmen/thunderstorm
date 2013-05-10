package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.rendering.ASHRendering;
import cz.cuni.lf1.rendering.DensityRendering;
import cz.cuni.lf1.rendering.HistogramRendering;
import cz.cuni.lf1.rendering.RenderingMethod;
import cz.cuni.lf1.rendering.ScatterRendering;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class RenderingPlugIn implements PlugIn {

  private static final String[] METHODS = new String[]{"Density", "ASH", "Histogram", "Scatter"};
  private static final String LABEL_X_POS = "x";
  private static final String LABEL_Y_POS = "y";

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
    if (xpos == null || ypos == null) {
      IJ.error("results were null");
      return;
    }

    GenericDialog gd = new GenericDialog("New Image");
    gd.addChoice("Method", METHODS, "ASH");
    gd.addNumericField("Image_size_X", max(xpos), 2);
    gd.addNumericField("Image_size_Y", max(ypos), 2);
    gd.addNumericField("Resolution", 0.2, 3);

    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    }
    String selectedMethod = gd.getNextChoice();
    int imSizeX = (int) gd.getNextNumber();
    int imSizeY = (int) gd.getNextNumber();
    double resolution = gd.getNextNumber();

    RenderingMethod renderer;
    if ("Density".equals(selectedMethod)) {
      renderer = new DensityRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).build();
    } else if ("ASH".equals(selectedMethod)) {
      renderer = new ASHRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).shifts(2).build();
    } else if ("Histogram".equals(selectedMethod)) {
      renderer = new HistogramRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).build();
    } else if ("Scatter".equals(selectedMethod)) {
      renderer = new ScatterRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).build();
    } else {
      IJ.error("Unknown rendering method. " + selectedMethod);
      return;
    }
    new ImagePlus(renderer.getClass().getSimpleName(), renderer.getRenderedImage(xpos, ypos, 0.2)).show();

  }

  private double max(double[] arr) {
    double max = arr[0];
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      }
    }
    return max;
  }
}
