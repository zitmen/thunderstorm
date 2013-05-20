package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.util.CSV;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

public class LeastSquaresEstimatorTest {

    class Pair {
        Point detection;
        PSF fit;
        PSF ground_truth;
        
        public Pair(Point detection, PSF fit, PSF ground_truth) {
            this.detection = detection;
            this.fit = fit;
            this.ground_truth = ground_truth;
        }
    }
    
    private Vector<Pair> pairFitsAndDetections2GroundTruths(Vector<Point> detections, Vector<PSF> fits, Vector<PSF> ground_truth) {
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
            best_dist2 = dist2(fits.elementAt(i), ground_truth.elementAt(best_fit));
            for(int j = 1, jm = ground_truth.size(); j < jm; j++) {
                dist2 = dist2(fits.elementAt(i), ground_truth.elementAt(j));
                if(dist2 < best_dist2) {
                    best_dist2 = dist2;
                    best_fit = j;
                }
            }
            pairs.add(new Pair(detections.elementAt(i), fits.elementAt(i), ground_truth.elementAt(best_fit)));
        }
        
        return pairs;
    }
    
    private double dist2(Point detection, PSF ground_truth) {
        return sqr(detection.x.doubleValue() - ground_truth.xpos) + sqr(detection.y.doubleValue() - ground_truth.ypos);
    }
    
    private double dist2(PSF fit, PSF ground_truth) {
        return sqr(fit.xpos - ground_truth.xpos) + sqr(fit.ypos - ground_truth.ypos);
    }
    
    /**
     * Test of estimateParameters method, of class LeastSquaresEstimator.
     */
    @Test
    public void testEstimateParameters() throws ThresholdFormulaException {
        System.out.println("LeastSquaresEstimator::estimateParameters");
        
        try {
            FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/tubulins1_00020.tif").getProcessor().convertToFloat();
            FloatProcessor filtered = (new CompoundWaveletFilter(false)).filterImage(image);
            Vector<Point> detections = (new CentroidOfConnectedComponentsDetector(false, "14.2835")).detectMoleculeCandidates(filtered);
            Vector<PSF> fits = (new LeastSquaresEstimator(11)).estimateParameters(image, detections);
            for(PSF fit : fits) {
                fit.convertXYToNanoMeters(150.0);
            }
            Vector<PSF> ground_truth = CSV.csv2psf("test/resources/tubulins1_00020.csv", 1, 2);
            Vector<Pair> pairs = pairFitsAndDetections2GroundTruths(detections, fits, ground_truth);
            for(Pair pair : pairs) {
                assertFalse("Result from the estimator should be better than guess from the detector.", dist2(pair.fit, pair.ground_truth) > dist2(pair.detection, pair.ground_truth));
            }
            //
            // Note: better test would be to compare these results to the results from Matlab...but I don't have them at the moment
            //
        } catch(IOException ex) {
            fail("Error in LSE test (this test also involves CompoundWaveletFilter and CentroidOfConnectedComponentsDetector): " + ex.getMessage());
        }
    }

}