package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.*;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.MLE;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.WLSQ;

import javax.swing.*;

public class PhasorAstigmatismCalibrationEstimatorUI extends PhasorEstimatorUI implements ICalibrationEstimatorUI {

    private final String name = "Phasor Astigmatism";
    private double angle;
    private boolean angleWasSet = false;
    private DefocusFunction defocus = null;

    public PhasorAstigmatismCalibrationEstimatorUI() {
        super();
        crowdedField = new CrowdedFieldEstimatorUI() {
            @Override
            MFA_LSQFitter getLSQImplementation(PSFModel psf, double sigma) {
                return null;
            }

            @Override
            MFA_MLEFitter getMLEImplementation(PSFModel psf, double sigma) {
                return null;
            }

            @Override
            public void resetToDefaults() {
            }

            @Override
            public void readMacroOptions(String options) {
            }

            @Override
            public void recordOptions() {
            }

            @Override
            public void readParameters() {
            }
            
            @Override
            public JPanel getOptionsPanel(JPanel panel) {
                return panel;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    public void setAngle(double fixedAngle) {
        this.angle = fixedAngle;
        angleWasSet = true;
    }

    public void setDefocusModel(DefocusFunction defocus) {
        this.defocus = defocus;
    }

    public void unsetAngle() {
        angleWasSet = false;
    }

    public int getFitradius() {
        return parameters.getInt(FITRAD);
    }
    
    @Override
    public IEstimator getImplementation() {
        //String method = METHOD.getValue();
        //double sigma = SIGMA.getValue();
        int fitradius = FITRAD.getValue();
        //PSFModel psf = angleWasSet ? new EllipticGaussianPSF(sigma, angle) : new EllipticGaussianWAnglePSF(sigma, 0);
        return new MultipleLocationsImageFitting(fitradius, new PhasorFitter(fitradius));
        //throw new IllegalArgumentException("Unknown fitting method: " + method);
    }
}
