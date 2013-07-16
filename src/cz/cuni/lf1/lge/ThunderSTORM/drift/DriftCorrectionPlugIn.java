package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class DriftCorrectionPlugIn implements PlugIn {

  double[] x, y, frame;

  @Override
  public void run(String arg) {
    try {


      //load results window
      if (!IJResultsTable.isResultsWindow()) {
        IJ.showMessage("Requires ThunderSTORM results window open.");
        return;
      }
      getResultsFromTable();
      //show dialog
      GenericDialog dialog = new GenericDialog("Drift correction");
      dialog.addNumericField("Number_of_images rendered", 10, 0);
      dialog.addNumericField("Rendering magnification", 5, 2);
      dialog.addCheckbox("Show_plot", true);
      dialog.addCheckbox("Show_cross_correlations", false);
      dialog.showDialog();
      //load dialog values
      int bins = (int) dialog.getNextNumber();
      double magnification = dialog.getNextNumber();
      boolean showPlot = dialog.getNextBoolean();
      boolean showCorrelationImages = dialog.getNextBoolean();
      if (bins < 2) {
        throw new IllegalArgumentException("Number of images must be greater than 1. Input: " + bins);
      }
      if (magnification <= 0) {
        throw new IllegalArgumentException("Rendering magnification must be greater than 0. Input: " + magnification);
      }
      //calculate drift
      CrossCorrelationDriftCorrection driftCorrection = new CrossCorrelationDriftCorrection(x, y, frame, bins, magnification, -1, -1, showCorrelationImages);
      //show plot
      if (showPlot) {
        showPlot(driftCorrection);
      }
      if (showCorrelationImages) {
        new ImagePlus("Cross correlations", driftCorrection.getCorrelationImages()).show();
      }
      //TODO: update results window


    } catch (Exception e) {
      IJ.handleException(e);
    }
  }

  void getResultsFromTable() {
    IJResultsTable rt = IJResultsTable.getResultsTable();
    if (!rt.columnExists(PSFInstance.X) || !rt.columnExists(PSFInstance.Y)) {
      throw new RuntimeException("Could not find " + PSFInstance.X + " and " + PSFInstance.Y + " columns.");
    }
    if (!rt.columnExists("frame")) {
      throw new RuntimeException("Could not find \"frame\" column.");
    }
    x = rt.getColumnAsDoubles(rt.getColumnIndex(PSFInstance.X));
    y = rt.getColumnAsDoubles(rt.getColumnIndex(PSFInstance.Y));
    frame = rt.getColumnAsDoubles(rt.getColumnIndex("frame"));
  }

  private static void showPlot(CrossCorrelationDriftCorrection driftCorrection) {
    int minFrame = driftCorrection.getMinFrame();
    int frameCount = driftCorrection.getMaxFrame() - minFrame;
    double[] grid = new double[frameCount];
    double[] driftX = new double[frameCount];
    double[] driftY = new double[frameCount];
    for (int i = 0; i < frameCount; i++) {
      grid[i] = i + minFrame;
      Point2D.Double offset = driftCorrection.getInterpolatedDrift(grid[i]);
      driftX[i] = offset.x;
      driftY[i] = offset.y;
    }
    Plot plot = new Plot("drift", "frame", "drift", grid, driftX);
    plot.setColor(Color.blue);
    plot.addPoints(driftCorrection.getBinCenters(), driftCorrection.getBinDriftX(), Plot.CROSS);
    plot.addLabel(0.05, 0.8, "x drift");
    plot.draw();
    plot.setColor(Color.red);
    plot.addPoints(grid, driftY, Plot.LINE);
    plot.addPoints(driftCorrection.getBinCenters(), driftCorrection.getBinDriftY(), Plot.CROSS);
    plot.addLabel(0.05, 0.9, "y drift");
    plot.show();
  }
}
