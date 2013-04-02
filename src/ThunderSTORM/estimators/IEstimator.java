package ThunderSTORM.estimators;

import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public interface IEstimator {
    
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections);
    
}
