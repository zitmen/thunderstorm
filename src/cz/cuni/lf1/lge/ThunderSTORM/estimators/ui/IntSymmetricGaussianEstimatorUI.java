package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;

public class IntSymmetricGaussianEstimatorUI extends SymmetricGaussianEstimatorUI {

    public IntSymmetricGaussianEstimatorUI() {
        super();
        name = "PSF: Integrated Gaussian";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IEstimator getImplementation() {
        String method = METHOD.getValue();
        double sigma = SIGMA.getValue();
        int fitradius = FITRAD.getValue();
        IntegratedSymmetricGaussianPSF psf = new IntegratedSymmetricGaussianPSF(sigma);
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                return crowdedField.getLSQImplementation(psf, sigma, fitradius);
            } else {
                LSQFitter fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
                return new MultipleLocationsImageFitting(fitradius, fitter);
            }
        }
        if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                return crowdedField.getMLEImplementation(psf, sigma, fitradius);
            } else {
                MLEFitter fitter = new MLEFitter(psf, Params.BACKGROUND);
                return new MultipleLocationsImageFitting(fitradius, fitter);
            }
        }
        throw new IllegalArgumentException("Unknown fitting method: " + method);
    }
}
