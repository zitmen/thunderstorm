package ThunderSTORM.utils;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConvolutionTest {
    
    /**
     * Test of Convolve method, of class Convolution.
     */
    @Test
    public void testConvolve() {
        System.out.println("Convolve");
        FloatProcessor image = null;
        FloatProcessor kernel = null;
        int padding_type = 0;
        FloatProcessor expResult = null;
        FloatProcessor result = Convolution.Convolve(image, kernel, padding_type);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSeparableKernelFromVectors method, of class Convolution.
     */
    @Test
    public void testGetSeparableKernelFromVectors() {
        System.out.println("getSeparableKernelFromVectors");
        float[] kx = null;
        float[] ky = null;
        FloatProcessor expResult = null;
        FloatProcessor result = Convolution.getSeparableKernelFromVectors(kx, ky);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}