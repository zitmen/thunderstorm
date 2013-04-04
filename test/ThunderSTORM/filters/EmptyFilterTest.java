package ThunderSTORM.filters;

import ij.IJ;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class EmptyFilterTest {
    
    /**
     * Test of filterImage method, of class EmptyFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("EmptyFilter::filterImage");
        FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/rice.png").getProcessor().convertToFloat();
        EmptyFilter instance = new EmptyFilter();
        FloatProcessor result = instance.filterImage(image);
        assertEquals(image, result);
    }
}