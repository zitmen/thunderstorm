package ThunderSTORM.detectors;

import ij.process.FloatProcessor;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class CentroidOfConnectedComponentsDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class CentroidOfConnectedComponentsDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() {
        System.out.println("CentroidOfConnectedComponentsDetector::detectMoleculeCandidates");
        
        FloatProcessor image = null;
        CentroidOfConnectedComponentsDetector instance = null;
        Vector expResult = null;
        Vector result = instance.detectMoleculeCandidates(image);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}