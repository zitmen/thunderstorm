package ThunderSTORM.detectors;

import ij.process.FloatProcessor;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

public class LocalMaximaDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class LocalMaximaDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() {
        System.out.println("detectMoleculeCandidates");
        FloatProcessor image = null;
        LocalMaximaDetector instance = null;
        Vector expResult = null;
        Vector result = instance.detectMoleculeCandidates(image);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}