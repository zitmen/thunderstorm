package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 *
 */
public class EmptyEstimator implements IEstimator {

    /**
     *
     * @param fp
     * @param detections
     * @return
     */
    @Override
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
