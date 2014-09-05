package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Z;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public class CylindricalLensZEstimator implements IEstimator {

    IEstimator estimator;

    public CylindricalLensZEstimator(IEstimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public Vector<Molecule> estimateParameters(FloatProcessor image, Vector<Point> detections) throws StoppedByUserException {
        return estimator.estimateParameters(image, detections);
    }
}
