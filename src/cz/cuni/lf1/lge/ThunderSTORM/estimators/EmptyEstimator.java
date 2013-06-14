package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * This estimator does no estimation, it just packs {@code Point}s found in
 * detection phase into {@code PSFModel} objects.
 *
 * This is suitable for quick preview of filtering/detection or for some basic
 * molecule counting applications where the pixel precision is not an issue.
 */
public class EmptyEstimator implements IEstimator, IEstimatorUI {

  public static final String[] names = new String[]{PSFInstance.X, PSFInstance.Y};

  @Override
  public Vector<PSFInstance> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
    Vector<PSFInstance> locations = new Vector<PSFInstance>();

    for (Point detection : detections) {
      locations.add(new PSFInstance(names, new double[]{detection.x.doubleValue(), detection.y.doubleValue()}));
    }
    return locations;
  }

  @Override
  public String getName() {
    return "No estimator";
  }

  @Override
  public JPanel getOptionsPanel() {
    return null;
  }

  @Override
  public void readParameters() {
    // nothing to do here
  }

  @Override
  public void recordOptions() {
  }

  @Override
  public void readMacroOptions(String options) {
  }

  @Override
  public IEstimator getImplementation() {
    return this;
  }
}
