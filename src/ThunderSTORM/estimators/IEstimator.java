package ThunderSTORM.estimators;

import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public interface IEstimator {
    
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections, PSF initial_guess);
    
}
