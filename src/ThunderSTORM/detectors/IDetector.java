package ThunderSTORM.detectors;

import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public interface IDetector {
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image);
}
