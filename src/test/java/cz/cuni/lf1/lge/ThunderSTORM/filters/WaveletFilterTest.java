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
            FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "rice.png").getProcessor().convertToFloat();
            
            WaveletFilter instance = new WaveletFilter(1, 3, 2.0, 5);
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp(basePath + "rice_filter_wavelet-V1.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
            
            instance = new WaveletFilter(2, 3, 2.0, 5);
            result = (float[]) instance.filterImage(image).getPixels();
            expResult = (float[]) CSV.csv2fp(basePath + "rice_filter_wavelet-V2.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
            
            instance = new WaveletFilter(3, 3, 2.0, 5);
            result = (float[]) instance.filterImage(image).getPixels();
            expResult = (float[]) CSV.csv2fp(basePath + "rice_filter_wavelet-V3.csv").getPixels();
            assertArrayEquals(expResult, result, 0.01f);
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }

}