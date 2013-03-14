package ThunderSTORM.detectors;

import ThunderSTORM.utils.Graph;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public class LocalMaximaDetector implements IDetector {

    private int connectivity;
    private double threshold;
    
    public LocalMaximaDetector(int connectivity, double threshold) {
        assert((connectivity == Graph.CONNECTIVITY_4) || (connectivity == Graph.CONNECTIVITY_8));
        
        this.connectivity = connectivity;
        this.threshold = threshold;
    }

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        return detections;
    }
}
