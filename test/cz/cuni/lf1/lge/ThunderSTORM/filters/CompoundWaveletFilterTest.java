package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.utils.CSV;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class CompoundWaveletFilterTest {
    
    /**
     * Test of filterImage method, of class CompoundWaveletFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("CompoundWaveletFilter::filterImage");
        
        try {
            FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/rice.png").getProcessor().convertToFloat();
            
            CompoundWaveletFilter instance = new CompoundWaveletFilter(false);
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp("test/resources/rice_filter_compound-wavelet-V1-V2.csv").getPixels();
            assertArrayEquals(expResult, result, 0.001f);
            
            instance = new CompoundWaveletFilter(true);
            result = (float[]) instance.filterImage(image).getPixels();
            expResult = (float[]) CSV.csv2fp("test/resources/rice_filter_compound-wavelet-V2-V3.csv").getPixels();
            assertArrayEquals(expResult, result, 0.001f);
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }

}