package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import java.awt.geom.Point2D;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

public class DriftResults {

    //interpolated drift
    private PolynomialSplineFunction xFunction;
    private PolynomialSplineFunction yFunction;
    //actual estimated drift data
    private double[] driftDataFrame;
    private double[] driftDataX;
    private double[] driftDataY;
    //
    private int minFrame;
    private int maxFrame;
    //units of the drift, both for original values interpolation 
    MoleculeDescriptor.Units units;

    public DriftResults(PolynomialSplineFunction xFunction,
            PolynomialSplineFunction yFunction,
            double[] driftDataFrame,
            double[] driftDataX,
            double[] driftDataY,
            int minFrame, int maxFrame,
            MoleculeDescriptor.Units units) {
        this.xFunction = xFunction;
        this.yFunction = yFunction;
        this.driftDataFrame = driftDataFrame;
        this.driftDataX = driftDataX;
        this.driftDataY = driftDataY;
        this.minFrame = minFrame;
        this.maxFrame = maxFrame;
        this.units = units;
    }

    public int getBinCount() {
        return driftDataFrame.length;
    }

    public double[] getDriftDataFrame() {
        return driftDataFrame;
    }

    public double[] getDriftDataX() {
        return driftDataX;
    }

    public double[] getDriftDataY() {
        return driftDataY;
    }

    public int getMinFrame() {
        return minFrame;
    }

    public int getMaxFrame() {
        return maxFrame;
    }

    public Point2D.Double getInterpolatedDrift(double frameNumber) {
        return new Point2D.Double(xFunction.value(frameNumber), yFunction.value(frameNumber));
    }

    public MoleculeDescriptor.Units getUnits() {
        return units;
    }
}
