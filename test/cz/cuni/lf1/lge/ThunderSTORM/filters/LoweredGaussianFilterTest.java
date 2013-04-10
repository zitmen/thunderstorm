package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class LoweredGaussianFilterTest {
    
    /**
     * Test of filterImage method, of class LoweredGaussianFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("LoweredGaussianFilter::filterImage");
        
        float [] result, expResult;
        LoweredGaussianFilter instance;
        FloatProcessor image = new FloatProcessor(new float [][] {
            { 17f, 23f,  4f, 10f, 11f },
            { 24f,  5f,  6f, 12f, 18f },
            {  1f,  7f, 13f, 19f, 25f },
            {  8f, 14f, 20f, 21f,  2f },
            { 15f, 16f, 22f,  3f,  9f }
        });
        
        expResult = new float [] {
            2.5433f, 3.5654f, 3.9482f, 3.5884f, 2.5765f,
            3.5843f, 4.9683f, 5.5511f, 5.1350f, 3.8273f,
            3.9773f, 5.5686f, 6.2664f, 5.8041f, 4.3206f,
            3.6106f, 5.1598f, 5.8216f, 5.3266f, 3.8536f,
            2.5820f, 3.8496f, 4.3497f, 3.8726f, 2.6152f
        };
        instance = new LoweredGaussianFilter(13, 2.0, Padding.PADDING_ZERO);
        result = (float[])instance.filterImage(image).getPixels();
        assertArrayEquals("Gaussian blur (sigma=2.0) failed!", expResult, result, 0.0001f);
    }

}