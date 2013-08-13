package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Z;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
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
    public Vector<Molecule> estimateParameters(FloatProcessor image, Vector<Point> detections) {
        Vector<Molecule> results = estimator.estimateParameters(image, detections);
        for(int i = 0; i < results.size(); i++) {
            Molecule psf = results.get(i);
            double sigma1 = psf.getParam(LABEL_SIGMA1);
            double sigma2 = psf.getParam(LABEL_SIGMA2);
            double calculatedZ = calibration.getZ(sigma1, sigma2);
            results.set(i, appendZ(psf, calculatedZ));
        }
        return results;
    }

    private static Molecule appendZ(Molecule mol, double zValue) {
        mol.insertParamAt(2, LABEL_Z, Units.LABEL_NANOMETER, zValue);   // [0]=>x,[1]=>y,[2]=>z
        return mol;
    }
}
