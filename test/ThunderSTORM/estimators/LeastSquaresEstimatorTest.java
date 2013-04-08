package ThunderSTORM.estimators;

import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
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
        
        FloatProcessor image = null;
        Vector<Point> detections = null;
        LeastSquaresEstimator instance = null;
        Vector expResult = null;
        Vector result = instance.estimateParameters(image, detections);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}