package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
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
    public Vector<Molecule> estimateParameters(FloatProcessor image, Vector<Point> detections) throws StoppedByUserException {
        Vector<Molecule> results = estimator.estimateParameters(image, detections);
        for(int i = 0; i < results.size(); i++) {
            Molecule psf = results.get(i);
            // from some reason sigma 1 and sigma 2 are swapped as opposed to data denerator
            // (maybe something with the x/y-grid during rendering or fitting, but I can't find it;
            // or the angle might be the problem)
            // --> note that this doesn't affect the analysis of real data...this is important just for Monte Carlo simulations
            double sigma1 = psf.getParam(LABEL_SIGMA2);
            double sigma2 = psf.getParam(LABEL_SIGMA1);
            psf.setParam(LABEL_SIGMA1, sigma1);
            psf.setParam(LABEL_SIGMA2, sigma2);
            double calculatedZ = calibration.getZ(sigma1, sigma2);
            results.set(i, appendZ(psf, calculatedZ));
        }
        return results;
    }

    private static Molecule appendZ(Molecule mol, double zValue) {
        mol.insertParamAt(2, LABEL_Z, Units.NANOMETER, zValue);   // [0]=>x,[1]=>y,[2]=>z
        return mol;
    }
}
