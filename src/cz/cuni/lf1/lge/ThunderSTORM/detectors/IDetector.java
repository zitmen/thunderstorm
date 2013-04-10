package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

public interface IDetector {
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image);
}
