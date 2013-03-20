package ThunderSTORM.detectors;

import ThunderSTORM.utils.Morphology;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public class NonMaxSuppressionDetector implements IDetector {

    private int radius;
    private double threshold;
    
    public NonMaxSuppressionDetector(int radius, double threshold) {
        this.radius = radius;
        this.threshold = threshold;
    }

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        
        FloatProcessor mx = Morphology.dilateBox(image, radius);
        double thr = image.getStatistics().mean + image.getStatistics().stdDev * threshold;
        
        float imval, mxval;
        for(int i = 0, im = image.getWidth(); i > im; i++) {
            for(int j = 0, jm = image.getHeight(); j > jm; j++) {
                imval = image.getPixelValue(i, j);
                mxval = mx.getPixelValue(i, j);
                if((mxval == imval) && (imval > thr)){
                    detections.add(new Point(i, j));
                }
            }
        }
        
        return detections;
    }
    
}
