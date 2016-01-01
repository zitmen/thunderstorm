package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DoubleDefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.BiplaneEllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;

public class EllipticBiplaneGaussianEstimatorUI extends SymmetricBiplaneGaussianEstimatorUI {

    public EllipticBiplaneGaussianEstimatorUI() {
        this.name = "PSF: Elliptical Gaussian (3D biplane + astigmatism)";
    }

    @Override
    protected PSFModel getPSFModel(DefocusCalibration calibration, boolean numericalDerivatives) {
        //noinspection unchecked
        return new BiplaneEllipticGaussianPSF((DoubleDefocusCalibration<DaostormCalibration>) calibration, numericalDerivatives);
    }
}
