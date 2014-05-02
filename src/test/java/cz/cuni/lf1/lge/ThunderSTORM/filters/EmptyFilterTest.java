package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.IJ;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmptyFilterTest {
    
    /**
     * Test of filterImage method, of class EmptyFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("EmptyFilter::filterImage");
        String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "rice.png").getProcessor().convertToFloat();
        EmptyFilter instance = new EmptyFilter();
        FloatProcessor result = instance.filterImage(image);
        assertEquals(image, result);
    }
}