package cz.cuni.lf1.lge.ThunderSTORM.utils;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class PaddingTest {
    
    /**
     * Test of addBorder method, of class Padding.
     */
    @Test
    public void testAddBorder() {
        System.out.println("Padding::addBorder");
        
        float [] data = new float[] {
            8f, 1f, 6f,
            3f, 5f, 7f,
            4f, 9f, 2f
        };
        FloatProcessor image = new FloatProcessor(3, 3, data, null);
        FloatProcessor result;
        float [] expResult;
        
        result = Padding.addBorder(image, Padding.PADDING_NONE, 0);
        assertArrayEquals("PADDING_NONE", data, (float[])result.getPixels(), 0.0f);
        
        result = Padding.addBorder(image, Padding.PADDING_ZERO, 2);
        expResult = new float[] {
            0f, 0f, 0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f, 0f, 0f,
            0f, 0f, 8f, 1f, 6f, 0f, 0f,
            0f, 0f, 3f, 5f, 7f, 0f, 0f,
            0f, 0f, 4f, 9f, 2f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f, 0f, 0f,
            0f, 0f, 0f, 0f, 0f, 0f, 0f
        };
        assertArrayEquals("PADDING_ZERO", expResult, (float[])result.getPixels(), 0.0f);
        
        result = Padding.addBorder(image, Padding.PADDING_DUPLICATE, 2);
        expResult = new float[] {
            8f, 8f, 8f, 1f, 6f, 6f, 6f,
            8f, 8f, 8f, 1f, 6f, 6f, 6f,
            8f, 8f, 8f, 1f, 6f, 6f, 6f,
            3f, 3f, 3f, 5f, 7f, 7f, 7f,
            4f, 4f, 4f, 9f, 2f, 2f, 2f,
            4f, 4f, 4f, 9f, 2f, 2f, 2f,
            4f, 4f, 4f, 9f, 2f, 2f, 2f
        };
        assertArrayEquals("PADDING_DUPLICATE", expResult, (float[])result.getPixels(), 0.0f);
        
        result = Padding.addBorder(image, Padding.PADDING_CYCLIC, 5);
        expResult = new float[] {
            5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f,
            9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f,
            1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f,
            5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f,
            9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f,
            1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f,
            5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f,
            9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f,
            1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f,
            5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f,
            9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f, 2f, 4f, 9f,
            1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f, 6f, 8f, 1f,
            5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f, 7f, 3f, 5f
        };
        assertArrayEquals("PADDING_CYCLIC", expResult, (float[])result.getPixels(), 0.0f);
    }
}