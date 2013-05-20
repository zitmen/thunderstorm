package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 * The interface every detector has to implement.
 */
public interface IDetector {
    /**
     * Detect molecules in {@code image} and return list of their X,Y positions.
     *
     * @param image an input (filtered) image
     * @return a Vector of instances of Points with their X,Y coordinates set to
     *         the positions where possible modelules were found
     * 
     * @see Point
     */
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws ThresholdFormulaException;
}
