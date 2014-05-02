package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.CSV;
import ij.IJ;
import ij.process.FloatProcessor;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class CompoundWaveletFilterTest {
    
    /**
     * Test of filterImage method, of class CompoundWaveletFilter.
     */
    @Ignore // This test needs to be fixed, because it is not corresponding to the current implementation!
    @Test
    public void testFilterImage() {
        System.out.println("CompoundWaveletFilter::filterImage");
        try {
            String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            FloatProcessor image = (FloatProcessor) IJ.openImage(basePath + "resources/rice.png").getProcessor().convertToFloat();
            
            CompoundWaveletFilter instance = new CompoundWaveletFilter();
            float[] result = (float[]) instance.filterImage(image).getPixels();
            float[] expResult = (float[]) CSV.csv2fp(basePath + "resources/rice_filter_compound-wavelet-V1-V2.csv").getPixels();
            assertArrayEquals(expResult, result, 0.001f);
        } catch(IOException ex) {
            fail("Error in box filter test: " + ex.getMessage());
        }
    }
}