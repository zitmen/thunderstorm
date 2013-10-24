package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 * The interface every detector has to implement.
 */
public interface IDetector extends IModule{
    /**
     * Detect molecules in {@code image} and return list of their X,Y positions.
     * All detectors should support detection in ROI (Region Of Interest).
     * If the ROI is non-rectangle shape, then mask is not null and contains
     * an input image masked by the ROI.
     *
     * @param image an input (filtered) image
     * 
     * @see Point
     */
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException, StoppedByUserException;
    
    public String getThresholdFormula();
    
    public float getThresholdValue();
}
