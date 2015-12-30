package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;

import java.util.List;

/**
 * The interface every biplane estimator has to implement.
 */
public interface IBiplaneEstimator extends IModule {

    /**
     * Estimate parameters of individual molecules based on the initial rough
     * guessed positions from a detector and store the list of molecules with
     * their parameters in a vector of PSFs.
     *
     * @param plane1 an input (raw) first plane image
     * @param plane2 an input (raw) second plane image
     * @param detections1 list of detections from the first plane returned by a detector
     * @param detections2 list of detections from the second plane returned by a detector
     * @return a Vector of instances of PSFs that contain parameters of every
     * single molecule.
     *
     * @see PSFModel
     * @see Point
     * @see IDetector
     */
    public List<Molecule> estimateParameters(FloatProcessor plane1, FloatProcessor plane2, List<Point> detections1, List<Point> detections2) throws StoppedByUserException;
}
