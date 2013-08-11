package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.SIGMA2;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 */
public class CylindricalLensZEstimator implements IEstimator {

  CylindricalLensCalibration calibration;
  IEstimator estimator;

  public CylindricalLensZEstimator(CylindricalLensCalibration calibration, IEstimator estimator) {
    this.calibration = calibration;
    this.estimator = estimator;
  }
  
  @Override
  public Vector<PSFInstance> estimateParameters(FloatProcessor image, Vector<Point> detections) {
    Vector<PSFInstance> results = estimator.estimateParameters(image, detections);
    for (int i = 0; i < results.size(); i++) {
      PSFInstance psf = results.get(i);
      double sigma1 = psf.getParam(SIGMA1);
      double sigma2 = psf.getParam(SIGMA2);
      double calculatedZ = calibration.getZ(sigma1 , sigma2);
      results.set(i, appendZ(psf, calculatedZ));
    }
    return results;
  }

  private static PSFInstance appendZ(PSFInstance psf, double zValue) {
    int[] originalParams = psf.getParamIndices();
    double[] originalValues = psf.getParamArray();
    int[] newParams = Arrays.copyOf(originalParams, originalParams.length + 1);
    double[] newValues = Arrays.copyOf(originalValues, originalValues.length + 1);
    newParams[newParams.length - 1] = PSFModel.Params.Z;
    newValues[newValues.length - 1] = zValue;

    return new PSFInstance(new PSFModel.Params(newParams, newValues, true));
  }
}
