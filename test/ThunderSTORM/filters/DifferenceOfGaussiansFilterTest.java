package ThunderSTORM.filters;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class DifferenceOfGaussiansFilterTest {
    
    /**
     * Test of filterImage method, of class DifferenceOfGaussiansFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("DifferenceOfGaussiansFilter::filterImage");
        // TODO: test g1>g2, g2<g1, and g1==g2
        FloatProcessor image = null;
        DifferenceOfGaussiansFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.filterImage(image);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}