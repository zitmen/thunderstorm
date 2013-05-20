package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 * The interface every estimator has to implement.
 */
public interface IEstimator extends IModule{
    
    /**
     * Estimate parameters of individual molecules based on the initial rough guessed
     * positions from a detector and store the list of molecules with their parameters
     * in a vector of PSFs.
     *
     * @param image an input (raw) image
     * @param detections list of detections returned by a detector
     * @return a Vector of instances of PSFs that contain parameters of every
     *         single molecule ({@mathjax x,y,\sigma,I,b}). <strong>Note: </strong>
     *         in a future release the PSF will be more abstract to allow easily
     *         work with any possible PSF out there, but right we use strictly
     *         the symmetric 2D Gaussian model
     * 
     * @see PSF
     * @see Point
     * @see IDetector
     */
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections);
    
}
