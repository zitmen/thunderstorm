package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 */
public class CylindricalLensEstimator implements IEstimator {

  CylindricalLensCalibration calibration;
  IEstimator estimator;

  public CylindricalLensEstimator(CylindricalLensCalibration calibration, IEstimator estimator) {
    this.calibration = calibration;
    this.estimator = estimator;
  }
  
  @Override
  public Vector<PSFInstance> estimateParameters(FloatProcessor image, Vector<Point> detections) {
    Vector<PSFInstance> results = estimator.estimateParameters(image, detections);
    for (int i = 0; i < results.size(); i++) {
      PSFInstance psf = results.get(i);
      double sigma1 = psf.getParam(PSFInstance.SIGMA);
      double sigma2 = psf.getParam(PSFInstance.SIGMA2);
      double calculatedZ = calibration.getZ(sigma1 / sigma2);
      results.set(i, appendZ(psf, calculatedZ));
    }
    return results;
  }

  private static PSFInstance appendZ(PSFInstance psf, double zValue) {
    String[] originalNames = psf.getParamNames();
    double[] originalValues = psf.getParamArray();
    String[] newNames = Arrays.copyOf(originalNames, originalNames.length + 1);
    double[] newValues = Arrays.copyOf(originalValues, originalValues.length + 1);
    newNames[newNames.length - 1] = PSFInstance.Z;
    newValues[newNames.length - 1] = zValue;

    return new PSFInstance(newNames, newValues);
  }
}
