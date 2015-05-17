package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.FullImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
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
        method = METHOD.getValue();
        initialSigma = SIGMA.getValue();
        fittingRadius = FITRAD.getValue();
        fullImageFitting = FULL_IMAGE_FITTING.getValue();
        IntegratedSymmetricGaussianPSF psf = new IntegratedSymmetricGaussianPSF(initialSigma);
        OneLocationFitter fitter;
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getLSQImplementation(psf, initialSigma);
            } else {
                fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
            }
        } else if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitter(psf, Params.BACKGROUND);

            }
        } else {
            throw new IllegalArgumentException("Unknown fitting method: " + method);
        }
        if(fullImageFitting) {
            return new FullImageFitting(fitter);
        } else {
            return new MultipleLocationsImageFitting(fittingRadius, fitter);
        }

    }
}
