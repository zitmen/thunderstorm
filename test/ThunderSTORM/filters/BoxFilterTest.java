package ThunderSTORM.filters;

import ThunderSTORM.utils.CSV;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class BoxFilterTest {
    
    /**
     * Test of filterImage method, of class MedianFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("BoxFilter::filterImage");
        
        try {
            FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/rice.png").getProcessor().convertToFloat();
            BoxFilter instance = new BoxFilter(5);
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp("test/resources/rice_filter_box5.csv").getPixels();
            assertArrayEquals(expResult, result, 0.0001f);
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }

}