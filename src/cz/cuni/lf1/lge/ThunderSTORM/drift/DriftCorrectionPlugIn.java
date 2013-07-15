package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class DriftCorrectionPlugIn implements PlugIn {

  double[] x, y, frame;

  @Override
  public void run(String arg) {
    getResultsFromTable();
    CrossCorrelationDriftCorrection driftCorrection = new CrossCorrelationDriftCorrection(x, y, frame, 10, 5, -1, -1);
    IJ.log(Arrays.toString(driftCorrection.binDriftX));
    IJ.log(Arrays.toString(driftCorrection.binDriftY));

    int maxFrame = (int) frame[frame.length - 1];
    showPlot(maxFrame, driftCorrection);

  }

  void getResultsFromTable() {
    IJResultsTable rt = IJResultsTable.getResultsTable();
    x = rt.getColumnAsDoubles(rt.getColumnIndex(PSFInstance.X));
    y = rt.getColumnAsDoubles(rt.getColumnIndex(PSFInstance.Y));
    frame = rt.getColumnAsDoubles(rt.getColumnIndex("frame"));
  }

  private void showPlot(int maxFrame, CrossCorrelationDriftCorrection driftCorrection) {
    double[] frames = new double[maxFrame];
    double[] dx = new double[maxFrame];
    double[] dy = new double[maxFrame];
    for (int i = 0; i < maxFrame; i++) {
      frames[i] = i + 1;
      Point2D.Double offset = driftCorrection.getFrameOffset(i + 1);
      dx[i] = offset.x;
      dy[i] = offset.y;
    }
    Plot plot = new Plot("drift", "frame", "delta", frames, dx);
    plot.setColor(Color.blue);
    plot.addPoints(driftCorrection.binCenters, driftCorrection.binDriftX, Plot.CROSS);
    plot.addLabel(0.05, 0.8, "x drift");
    plot.draw();
    plot.setColor(Color.red);
    plot.addPoints(frames, dy, Plot.LINE);
    plot.addPoints(driftCorrection.binCenters, driftCorrection.binDriftY, Plot.CROSS);
    plot.addLabel(0.05, 0.9, "y drift");
    plot.show();
  }
}
