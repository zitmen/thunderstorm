package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConvolutionFilterTest {
    
    float [] kernel, kernel_x, kernel_y;
    
    /**
     *
     */
    @Before
    public void prepareTest() {
        kernel = new float [] {
            0.03f, 0.06f, 0.09f, 0.06f, 0.03f,
            0.02f, 0.04f, 0.06f, 0.04f, 0.02f,
            0.01f, 0.02f, 0.03f, 0.02f, 0.01f,
            0.02f, 0.04f, 0.06f, 0.04f, 0.02f,
            0.03f, 0.06f, 0.09f, 0.06f, 0.03f
        };
        kernel_x = new float [] { 0.1f, 0.2f, 0.3f, 0.2f, 0.1f };
        kernel_y = new float [] { 0.3f, 0.2f, 0.1f, 0.2f, 0.3f };
    }
    
    private void testGetKernels(ConvolutionFilter instance, boolean separable, float [] k, float [] kx, float [] ky) {
        if(separable == true) {
            if(instance.getKernel() != null) fail("Separable kernel can't be defined by a 2D kernel matrix!");
            if(instance.getKernelX() == null) fail("Separable kernel has to be defined by a X and Y vector kernels!");
            if(instance.getKernelY() == null) fail("Separable kernel has to be defined by a X and Y vector kernels!");
            assertArrayEquals("Error in assigning the X vector kernel!", (float[])instance.getKernelX().getPixels(), kx, 0.0f);
            assertArrayEquals("Error in assigning the Y vector kernel!", (float[])instance.getKernelY().getPixels(), ky, 0.0f);
        } else {
            if(instance.getKernel() == null) fail("Non-separable kernel has to be defined by a 2D kernel matrix!");
            if(instance.getKernelX() != null) fail("Non-separable kernel can't be defined by a vector kernel!");
            if(instance.getKernelY() != null) fail("Non-separable kernel can't be defined by a vector kernel!");
            assertArrayEquals("Error in assigning the 2D kernel matrix!", (float[])instance.getKernel().getPixels(), k, 0.0f);
        }
    }
    
    /**
     * Test of updateKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testUpdateKernel() {
        System.out.println("ConvolutionFilter::updateKernel");
        
        ConvolutionFilter instance = new ConvolutionFilter(null, null, Padding.PADDING_NONE);
        instance.updateKernel(new FloatProcessor(5, 1, kernel_x, null), new FloatProcessor(1, 5, kernel_y, null));
        testGetKernels(instance, true, null, kernel_x, kernel_y);
        instance.updateKernel(new FloatProcessor(5, 1, kernel_x, null), true);
        testGetKernels(instance, true, null, kernel_x, kernel_x);
        instance.updateKernel(new FloatProcessor(5, 5, kernel, null), false);
        testGetKernels(instance, false, kernel, null, null);
    }

    /**
     * Test of filterImage method, of class ConvolutionFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("ConvolutionFilter::filterImage");
        
        FloatProcessor image = new FloatProcessor(new float[][] {
            {17f, 24f,  1f,  8f, 15f},
            {23f,  5f,  7f, 14f, 16f},
            { 4f,  6f, 13f, 20f, 22f},
            {10f, 12f, 19f, 21f,  3f},
            {11f, 18f, 25f,  2f,  9f}
        });
        ConvolutionFilter instance = new ConvolutionFilter(new FloatProcessor(5, 5, kernel, null), false, Padding.PADDING_ZERO);
        FloatProcessor expResult = instance.filterImage(image);
        instance.updateKernel(new FloatProcessor(5, 1, kernel_x, null), new FloatProcessor(1, 5, kernel_y, null));
        FloatProcessor result = instance.filterImage(image);
        assertArrayEquals("In case of a separable kernel, the result of convolution with 2D kernel matrix " +
                          "has to be the same as the result of convolution with X and Y vector kernels from " +
                          "which is the 2D matrix kernel compounded.",
                          (float[])expResult.getPixels(), (float[])result.getPixels(), 0.0001f);
    }

}