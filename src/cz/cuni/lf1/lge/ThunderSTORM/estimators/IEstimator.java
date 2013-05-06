package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public interface IEstimator {
    
    /**
     *
     * @param image
     * @param detections
     * @return
     */
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections);
    
}
