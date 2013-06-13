package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class AngleFittingEstimatorUI extends SymmetricGaussianEstimatorUI {

  @Override
  public String getName() {
    return "Elliptic Gaussian w/ angle";
  }

  @Override
  public IEstimator getImplementation() {
    if (LSQ.equals(method)) {
      LSQFitter fitter = new LSQFitter(new EllipticGaussianWAnglePSF(sigma, 0));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    if (MLE.equals(method)) {
      MLEFitter fitter = new MLEFitter(new EllipticGaussianWAnglePSF(sigma, 0));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    throw new IllegalArgumentException("Unknown fitting method: " + method);
  }

  public IEstimator getFixedAngleImplementation() {
    if (LSQ.equals(method)) {
      LSQFitter fitter = new LSQFitter(new EllipticGaussianPSF(sigma, 0));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    if (MLE.equals(method)) {
      MLEFitter fitter = new MLEFitter(new EllipticGaussianPSF(sigma, 0));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    throw new IllegalArgumentException("Unknown fitting method: " + method);
  }
}
