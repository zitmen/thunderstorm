package ThunderSTORM.estimators;

import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public class EmptyEstimator implements IEstimator {

    @Override
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
