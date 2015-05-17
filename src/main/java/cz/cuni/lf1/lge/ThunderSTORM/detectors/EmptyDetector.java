package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;

import javax.swing.*;
import java.util.Vector;


/**
 * This class is actually never used. The only purpose is for MeasurementProtocol, when no protocol is loaded.
 */
public class EmptyDetector extends IDetectorUI implements IDetector {

    private final String name = "No detector";

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException, StoppedByUserException {
        return new Vector<Point>();
    }

    @Override
    public String getThresholdFormula() {
        return "";
    }

    @Override
    public float getThresholdValue() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        return new JPanel();
    }

    @Override
    public IDetector getImplementation() {
        return null;
    }
}
