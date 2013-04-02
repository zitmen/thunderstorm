package ThunderSTORM.filters;

import ij.IJ;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class MedianFilterTest {
    
    /**
     * Test of filterImage method, of class MedianFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("filterImage");
        
        FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/rice.png").getProcessor().convertToFloat();
        FloatProcessor expResult = (FloatProcessor) IJ.openImage("test/resources/rice_median_cross3.png").getProcessor().convertToFloat();
        MedianFilter instance = new MedianFilter(MedianFilter.CROSS, 3);
        FloatProcessor result = instance.filterImage(image);
        assertEquals("Median - CROSS!!", expResult, result);
        
        expResult = (FloatProcessor) IJ.openImage("test/resources/rice_median_box3.png").getProcessor().convertToFloat();
        instance = new MedianFilter(MedianFilter.BOX, 3);
        result = instance.filterImage(image);
        assertEquals("Median - BOX!!", expResult, result);
    }
}