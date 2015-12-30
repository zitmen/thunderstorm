package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;

public class EllipticBiplaneGaussianEstimatorUI extends SymmetricBiplaneGaussianEstimatorUI {

    public EllipticBiplaneGaussianEstimatorUI() {
        this.name = "PSF: Elliptical Gaussian (3D biplane + astigmatism)";
    }

    @Override
    protected PSFModel getPSFModel(double initialSigma, DefocusCalibration calibration) {
        return new EllipticGaussianPSF(calibration);
    }
}
