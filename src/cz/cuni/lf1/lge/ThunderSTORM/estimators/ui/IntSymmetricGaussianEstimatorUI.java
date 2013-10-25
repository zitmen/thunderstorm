package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
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
        if(LSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                return crowdedField.getLSQImplementation(new IntegratedSymmetricGaussianPSF(sigma), sigma, fitradius);
            } else {
                LSQFitter fitter = new LSQFitter(new IntegratedSymmetricGaussianPSF(sigma));
                return new MultipleLocationsImageFitting(fitradius, fitter);
            }
        }
        if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                return crowdedField.getMLEImplementation(new IntegratedSymmetricGaussianPSF(sigma), sigma, fitradius);
            } else {
                MLEFitter fitter = new MLEFitter(new IntegratedSymmetricGaussianPSF(sigma));
                return new MultipleLocationsImageFitting(fitradius, fitter);
            }
        }
        throw new IllegalArgumentException("Unknown fitting method: " + method);
    }
}
