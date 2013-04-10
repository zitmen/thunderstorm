package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.utils.CSV;
import cz.cuni.lf1.lge.ThunderSTORM.utils.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class LeastSquaresEstimatorTest {
    
    /**
     * Test of estimateParameters method, of class LeastSquaresEstimator.
     */
    @Test
    public void testEstimateParameters() {
        System.out.println("LeastSquaresEstimator::estimateParameters");
        
        try {
            FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/tubulins1_00020.tif").getProcessor().convertToFloat();
            FloatProcessor filtered = (new CompoundWaveletFilter(false)).filterImage(image);
            Vector<Point> detections = (new CentroidOfConnectedComponentsDetector(false, 14.2835)).detectMoleculeCandidates(filtered);
            Vector<PSF> result = (new LeastSquaresEstimator(11)).estimateParameters(image, detections);
            for(PSF psf : result) {
                psf.convertXYToNanoMeters(150.0);
            }
            Vector<PSF> expResult = CSV.csv2psf("test/resources/tubulins1_00020.csv", 1, 2);
            Collections.sort(result, new PSF.XYZComparator());
            Collections.sort(expResult, new PSF.XYZComparator());
            //
            System.out.println("\n============= RESULT ===============");
            for(PSF psf : result) System.out.println(psf);
            System.out.println("\n\n======= EXPECTED RESULT ==========");
            for(PSF psf : expResult) System.out.println(psf);
            //
            // Ideally, this test should be evaluated by comparing to the results from Matlab,
            // however I don't have them right now, therefore the evaluation will go like this:
            // 1. detections = detect(image)
            // 2. fits = estimate(image,detections)
            // 3. for all fits: if fit.{x,y,I,s,b} is out of range, e.g., negative then FAIL!
            // 4. for all detections get dist2(detections)
            // 5. for all fits get dist2(fits)
            // 6. for all: if dist2(fits) > dist2(detections) then FAIL!
            //
            assertEquals(expResult, result);
        } catch(IOException ex) {
            fail("Error in LSE test (this test also involves CompoundWaveletFilter and CentroidOfConnectedComponentsDetector): " + ex.getMessage());
        }
    }

}