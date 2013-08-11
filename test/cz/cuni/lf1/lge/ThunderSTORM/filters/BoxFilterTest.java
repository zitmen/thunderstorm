package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.CSV;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoxFilterTest {
    
    /**
     * Test of filterImage method, of class MedianFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("BoxFilter::filterImage");
        
        try {
            String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "resources/rice.png").getProcessor().convertToFloat();
            BoxFilter instance = new BoxFilter(5);
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp(basePath + "resources/rice_filter_box5.csv").getPixels();
            assertArrayEquals(expResult, result, 0.001f);
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }

}