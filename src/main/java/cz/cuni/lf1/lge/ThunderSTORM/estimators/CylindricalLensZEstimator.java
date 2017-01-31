package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.thunderstorm.datastructures.Point2D;
import ij.process.FloatProcessor;

import java.util.List;

public class CylindricalLensZEstimator implements IEstimator {

    IEstimator estimator;

    public CylindricalLensZEstimator(IEstimator estimator) {
        this.estimator = estimator;
    }

    @Override
    public List<Molecule> estimateParameters(FloatProcessor image, List<Point2D> detections) throws StoppedByUserException {
        return estimator.estimateParameters(image, detections);
    }
}
