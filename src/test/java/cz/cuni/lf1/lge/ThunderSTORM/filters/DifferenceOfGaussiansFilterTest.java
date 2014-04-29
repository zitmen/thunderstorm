package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class DifferenceOfGaussiansFilterTest {
    
    /**
     * Test of filterImage method, of class DifferenceOfGaussiansFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("DifferenceOfGaussiansFilter::filterImage");
        
        float [] result, expResult;
        DifferenceOfGaussiansFilter instance;
        FloatProcessor image = new FloatProcessor(new float [][] {
            { 17f, 23f,  4f, 10f, 11f },
            { 24f,  5f,  6f, 12f, 18f },
            {  1f,  7f, 13f, 19f, 25f },
            {  8f, 14f, 20f, 21f,  2f },
            { 15f, 16f, 22f,  3f,  9f }
        });
        
        expResult = new float [] {
            3.4607f, 3.1853f, 1.1375f, 1.6170f, 1.9985f,
            3.2913f, 3.2552f, 2.7718f, 4.4676f, 4.1609f,
            1.4248f, 2.7960f, 4.5781f, 6.2434f, 4.4591f,
            1.6817f, 4.4367f, 6.2677f, 5.6491f, 2.5513f,
            1.7254f, 4.2256f, 4.7464f, 2.6573f, 0.2632f
        };
        instance = new DifferenceOfGaussiansFilter(13, 1.0, 2.0, Padding.PADDING_ZERO);
        result = (float[])instance.filterImage(image).getPixels();
        assertArrayEquals("DoG: g1<g2 failed!", expResult, result, 0.0001f);
        
        expResult = new float [] {
            -3.4607f, -3.1853f, -1.1375f, -1.6170f, -1.9985f,
            -3.2913f, -3.2552f, -2.7718f, -4.4676f, -4.1609f,
            -1.4248f, -2.7960f, -4.5781f, -6.2434f, -4.4591f,
            -1.6817f, -4.4367f, -6.2677f, -5.6491f, -2.5513f,
            -1.7254f, -4.2256f, -4.7464f, -2.6573f, -0.2632f
        };
        instance = new DifferenceOfGaussiansFilter(13, 2.0, 1.0, Padding.PADDING_ZERO);
        result = (float[])instance.filterImage(image).getPixels();
        assertArrayEquals("DoG: g1>g2 failed!", expResult, result, 0.0001f);
        
        expResult = new float [] {
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f
        };
        instance = new DifferenceOfGaussiansFilter(13, 2.0, 2.0, Padding.PADDING_ZERO);
        result = (float[])instance.filterImage(image).getPixels();
        assertArrayEquals("DoG: g1==g2 failed!", expResult, result, 0.0f);
    }
}