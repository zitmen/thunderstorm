package ThunderSTORM.filters;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompoundWaveletFilterTest {
    
    /**
     * Test of filterImage method, of class CompoundWaveletFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("filterImage");
        FloatProcessor image = null;
        CompoundWaveletFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.filterImage(image);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}