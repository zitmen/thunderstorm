package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public interface IDetector {
    /**
     *
     * @param image
     * @return
     */
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image);
}
