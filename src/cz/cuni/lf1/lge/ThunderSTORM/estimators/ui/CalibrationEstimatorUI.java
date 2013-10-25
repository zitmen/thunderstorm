package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;

public class CalibrationEstimatorUI extends SymmetricGaussianEstimatorUI {

    private final String name = "Elliptic Gaussian w/ angle";
    private double angle;
    private boolean angleWasSet = false;

    @Override
    public String getName() {
        return name;
    }

    public void setAngle(double fixedAngle) {
        this.angle = fixedAngle;
        angleWasSet = true;
    }

    public void unsetAngle() {
        angleWasSet = false;
    }

    public int getFitradius() {
        return parameters.getInt(FITRAD);
    }

    @Override
    public IEstimator getImplementation() {
        String method = METHOD.getValue();
        double sigma = SIGMA.getValue();
        int fitradius = FITRAD.getValue();
        PSFModel psf = angleWasSet ? new EllipticGaussianPSF(sigma, Math.toRadians(angle)) : new EllipticGaussianWAnglePSF(sigma, 0);
        if(LSQ.equals(method)) {
            LSQFitter fitter = new LSQFitter(psf);
            return new MultipleLocationsImageFitting(fitradius, fitter);
        }
        if(MLE.equals(method)) {
            MLEFitter fitter = new MLEFitter(psf);
            return new MultipleLocationsImageFitting(fitradius, fitter);
        }
        throw new IllegalArgumentException("Unknown fitting method: " + method);
    }
}
