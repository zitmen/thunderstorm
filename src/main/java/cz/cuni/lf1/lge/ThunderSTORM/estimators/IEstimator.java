
package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import java.util.List;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;

/**
 * The interface every estimator has to implement.
 */
public interface IEstimator extends IModule {

	/**
	 * Estimate parameters of individual molecules based on the initial rough
	 * guessed positions from a detector and store the list of molecules with
	 * their parameters in a vector of PSFs.
	 *
	 * @param image an input (raw) image
	 * @param detections list of detections returned by a detector
	 * @return a Vector of instances of PSFs that contain parameters of every
	 *         single molecule, say ({
	 * @mathjax x,y,\sigma,I,b}). <strong>Note: </strong> in a future release the
	 *          PSFModel will be more abstract to allow easily work with any
	 *          possible PSFModel out there, but right we use strictly the
	 *          symmetric 2D Gaussian model
	 * @see PSFModel
	 * @see Point
	 * @see IDetector
	 */
	List<Molecule> estimateParameters(FloatProcessor image, List<Point> detections)
		throws StoppedByUserException;
}
