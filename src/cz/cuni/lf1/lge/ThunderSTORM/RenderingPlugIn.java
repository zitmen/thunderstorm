package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ScatterRendering;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.UI;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

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
    UI.setLookAndFeel();
    //
    IJResultsTable rt = IJResultsTable.getResultsTable();
    if (rt == null || !IJResultsTable.isResultsWindow()) {
      IJ.error("Requires Results window open");
      return;
    }
    if (!rt.columnExists(LABEL_X_POS) || !rt.columnExists(LABEL_Y_POS)) {
      IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X_POS, LABEL_Y_POS, rt.getColumnHeadings()));
      return;
    }

    double[] xpos = rt.getColumnAsDoubles(rt.getColumnIndex(LABEL_X_POS));
    double[] ypos = rt.getColumnAsDoubles(rt.getColumnIndex(LABEL_Y_POS));
    if (xpos == null || ypos == null) {
      IJ.error("results were null");
      return;
    }

    GenericDialog gd = new GenericDialog("SMLM rendering");
    gd.addChoice("Method", METHODS, "ASH");
    gd.addNumericField("Image_size_X", Math.ceil(max(xpos)) + 2, 2);
    gd.addNumericField("Image_size_Y", Math.ceil(max(ypos)) + 2, 2);
    gd.addNumericField("Resolution", 0.2, 3);
    gd.addNumericField("dx (Localization accuracy)", 0.2, 3);
    gd.addNumericField("ASH_shifts", 2, 0);
    gd.addNumericField("Histogram_averages", 0, 0);

    Vector choices = gd.getChoices();
    final Choice ch = (Choice) choices.get(0);

    Vector numericFields = gd.getNumericFields();
    final TextField shiftsField = (TextField) numericFields.get(4);
    final TextField avgField = (TextField) numericFields.get(5);

    avgField.setEnabled(false);
    ch.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        String selected = (String) e.getItem();
        if ("ASH".equals(selected)) {
          avgField.setEnabled(false);
          shiftsField.setEnabled(true);
        } else if ("Histogram".equals(selected)) {
          avgField.setEnabled(true);
          shiftsField.setEnabled(false);
        } else {
          avgField.setEnabled(false);
          shiftsField.setEnabled(false);
        }
      }
    });

    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    }
    String selectedMethod = gd.getNextChoice();
    double imSizeX = gd.getNextNumber();
    double imSizeY = gd.getNextNumber();
    double resolution = gd.getNextNumber();
    double dx = gd.getNextNumber();
    int shifts = (int) gd.getNextNumber();
    int avg = (int) gd.getNextNumber();

    RenderingMethod renderer;
    if ("Density".equals(selectedMethod)) {
      renderer = new DensityRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).defaultDX(dx).build();
    } else if ("ASH".equals(selectedMethod)) {
      renderer = new ASHRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).defaultDX(dx).shifts(shifts).build();
    } else if ("Histogram".equals(selectedMethod)) {
      renderer = new HistogramRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).average(avg).defaultDX(dx).build();
    } else if ("Scatter".equals(selectedMethod)) {
      renderer = new ScatterRendering.Builder().resolution(resolution).roi(0, imSizeX, 0, imSizeY).defaultDX(dx).build();
    } else {
      IJ.error("Unknown rendering method. " + selectedMethod);
      return;
    }
    renderer.getRenderedImage(xpos, ypos, null, null).show();

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
