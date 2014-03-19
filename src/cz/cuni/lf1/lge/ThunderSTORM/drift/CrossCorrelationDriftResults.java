package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import ij.ImageStack;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CrossCorrelationDriftResults extends DriftResults {

    private transient ImageStack correlationImages;
    private double scale;

    public CrossCorrelationDriftResults(ImageStack correlationImages,
            PolynomialSplineFunction xFunction,
            PolynomialSplineFunction yFunction,
            double[] binCenters,
            double[] binDriftX,
            double[] binDriftY,
            double scale,
            int minFrame, int maxFrame,
            MoleculeDescriptor.Units units) {
        super(xFunction, yFunction, binCenters, binDriftX, binDriftY, minFrame, maxFrame, units);
        this.correlationImages = correlationImages;
        this.scale = scale;
    }

    public ImageStack getCorrelationImages() {
        return correlationImages;
    }

    public double getScaleFactor() {
        return scale;
    }
}
