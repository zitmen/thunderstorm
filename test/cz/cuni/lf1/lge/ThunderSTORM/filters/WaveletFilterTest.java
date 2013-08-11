package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.CSV;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class WaveletFilterTest {
    
    /**
     * Test of filterImage method, of class CompoundWaveletFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("WaveletFilter::filterImage");
        
        try {
            String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "resources/rice.png").getProcessor().convertToFloat();
            
            WaveletFilter instance = new WaveletFilter(1);
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp(basePath + "resources/rice_filter_wavelet-V1.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
            
            instance = new WaveletFilter(2);
            result = (float[]) instance.filterImage(image).getPixels();
            expResult = (float[]) CSV.csv2fp(basePath + "resources/rice_filter_wavelet-V2.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
            
            instance = new WaveletFilter(3);
            result = (float[]) instance.filterImage(image).getPixels();
            expResult = (float[]) CSV.csv2fp(basePath + "resources/rice_filter_wavelet-V3.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
            
            try {
                instance = new WaveletFilter(0);
                fail("Wavelet filter: planes < 1 should be by design unsupported!");
            } catch(IndexOutOfBoundsException ex) { }
            
            try {
                instance = new WaveletFilter(4);
                fail("Wavelet filter: planes > 3 should be by design unsupported!");
            } catch(IndexOutOfBoundsException ex) { }
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }

}