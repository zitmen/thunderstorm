package ThunderSTORM.detectors;

import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public class EmptyDetector implements IDetector {

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        return new Vector<Point>();
    }
    
}
