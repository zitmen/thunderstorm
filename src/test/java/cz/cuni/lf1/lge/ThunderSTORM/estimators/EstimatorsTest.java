package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.CSV;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;

import java.util.List;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

public class EstimatorsTest {

    @Test
    public void testRadialSymmetry() {
        testEstimator(new MultipleLocationsImageFitting(5, new RadialSymmetryFitter()));
    }

    @Test
    public void testLSQSym() {
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new SymmetricGaussianPSF(1), false, Params.BACKGROUND)));
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new SymmetricGaussianPSF(1), true, Params.BACKGROUND)));
    }

    @Test
    public void testLSQIntSym() {
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new IntegratedSymmetricGaussianPSF(1), false, Params.BACKGROUND)));
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new IntegratedSymmetricGaussianPSF(1), true, Params.BACKGROUND)));
    }

    @Test
    public void testMLEIntSym() {
        testEstimator(new MultipleLocationsImageFitting(5, new MLEFitter(new IntegratedSymmetricGaussianPSF(1), Params.BACKGROUND)));
    }

    @Test
    public void testMLESym() {
        testEstimator(new MultipleLocationsImageFitting(5, new MLEFitter(new SymmetricGaussianPSF(1), Params.BACKGROUND)));
    }

    @Test
    public void testLSQEllipticAngle() {
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new EllipticGaussianWAnglePSF(1, 0), false, Params.BACKGROUND)));
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new EllipticGaussianWAnglePSF(1, 0), true, Params.BACKGROUND)));
    }

    @Test
    public void testMLEEllipticAngle() {
        testEstimator(new MultipleLocationsImageFitting(5, new MLEFitter(new EllipticGaussianWAnglePSF(1, 0), Params.BACKGROUND)));
    }

    @Test
    public void testLSQElliptic() {
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new EllipticGaussianPSF(1, 45), false, Params.BACKGROUND)));
        testEstimator(new MultipleLocationsImageFitting(5, new LSQFitter(new EllipticGaussianPSF(1, 45), true, Params.BACKGROUND)));
    }

    @Test
    public void testMLEElliptic() {
        testEstimator(new MultipleLocationsImageFitting(5, new MLEFitter(new EllipticGaussianPSF(1, 45), Params.BACKGROUND)));
    }

    private void testEstimator(IEstimator estimator) throws FormulaParserException {

        String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "tubulins1_00020.tif").getProcessor().convertToFloat();
        FloatProcessor filtered = (new CompoundWaveletFilter()).filterImage(image);
        Vector<Point> detections = (new CentroidOfConnectedComponentsDetector("16", true)).detectMoleculeCandidates(filtered);
        List<Molecule> fits = estimator.estimateParameters(image, detections);
        for(Molecule fit : fits) {
            convertXYToNanoMeters(fit, 150.0);
        }
        Vector<Molecule> ground_truth = null;
        try {
            ground_truth = CSV.csv2psf(basePath + "tubulins1_00020.csv", 1, 2);
        } catch(Exception ex) {
            fail(ex.getMessage());
        }
        Vector<Pair> pairs = pairFitsAndDetections2GroundTruths(detections, fits, ground_truth);
        for(Pair pair : pairs) {
            assertFalse("Result from the estimator should be better than guess from the detector.", dist2(pair.fit, pair.ground_truth) > dist2(pair.detection, pair.ground_truth));
        }
        //
        // Note: better test would be to compare these results to the results from Matlab...but I don't have them at the moment
        //
    }

    static void convertXYToNanoMeters(Molecule fit, double px2nm) {
        fit.setX(fit.getX() * px2nm);
        fit.setY(fit.getY() * px2nm);
    }

    static class Pair {

        Point detection;
        Molecule fit;
        Molecule ground_truth;

        public Pair(Point detection, Molecule fit, Molecule ground_truth) {
            this.detection = detection;
            this.fit = fit;
            this.ground_truth = ground_truth;
        }
    }

    static Vector<Pair> pairFitsAndDetections2GroundTruths(Vector<Point> detections, List<Molecule> fits, Vector<Molecule> ground_truth) {
        assertNotNull(fits);
        assertNotNull(detections);
        assertNotNull(ground_truth);
        assertFalse(fits.isEmpty());
        assertFalse(detections.isEmpty());
        assertFalse(ground_truth.isEmpty());
        assertEquals("Number of detections should be the same as number of fits!", detections.size(), fits.size());

        Vector<Pair> pairs = new Vector<Pair>();
        int best_fit;
        double best_dist2, dist2;
        for(int i = 0, im = fits.size(); i < im; i++) {
            best_fit = 0;
            best_dist2 = dist2(fits.get(i), ground_truth.elementAt(best_fit));
            for(int j = 1, jm = ground_truth.size(); j < jm; j++) {
                dist2 = dist2(fits.get(i), ground_truth.elementAt(j));
                if(dist2 < best_dist2) {
                    best_dist2 = dist2;
                    best_fit = j;
                }
            }
            pairs.add(new Pair(detections.elementAt(i), fits.get(i), ground_truth.elementAt(best_fit)));
        }

        return pairs;
    }

    static double dist2(Point detection, Molecule ground_truth) {
        return sqr(detection.x.doubleValue() - ground_truth.getX()) + sqr(detection.y.doubleValue() - ground_truth.getY());
    }

    static double dist2(Molecule fit, Molecule ground_truth) {
        return sqr(fit.getX() - ground_truth.getY()) + sqr(fit.getX() - ground_truth.getY());
    }
}
