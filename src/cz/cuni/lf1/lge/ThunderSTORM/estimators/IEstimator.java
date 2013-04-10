package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public interface IEstimator {
    
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections);
    
}
