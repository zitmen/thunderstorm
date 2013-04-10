package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class GaussianFilterTest {
    
    /**
     * Test of filterImage method, of class GaussianFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("GaussianFilter::filterImage");
        
        float [] result, expResult;
        GaussianFilter instance;
        FloatProcessor image = new FloatProcessor(new float [][] {
            { 17f, 23f,  4f, 10f, 11f },
            { 24f,  5f,  6f, 12f, 18f },
            {  1f,  7f, 13f, 19f, 25f },
            {  8f, 14f, 20f, 21f,  2f },
            { 15f, 16f, 22f,  3f,  9f }
        });
        
        expResult = new float [] {
            4.4624f, 5.4845f, 5.8673f, 5.5075f, 4.4956f,
            5.5035f, 6.8874f, 7.4702f, 7.0542f, 5.7465f,
            5.8964f, 7.4878f, 8.1855f, 7.7232f, 6.2397f,
            5.5298f, 7.0790f, 7.7407f, 7.2458f, 5.7728f,
            4.5012f, 5.7687f, 6.2688f, 5.7917f, 4.5344f
        };
        instance = new GaussianFilter(13, 2.0, Padding.PADDING_ZERO);
        result = (float[])instance.filterImage(image).getPixels();
        assertArrayEquals("Gaussian blur (sigma=2.0) failed!", expResult, result, 0.0001f);
    }
    
}