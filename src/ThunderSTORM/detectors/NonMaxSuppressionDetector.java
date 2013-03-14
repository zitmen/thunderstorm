package ThunderSTORM.detectors;

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
        return detections;
    }
    
}
