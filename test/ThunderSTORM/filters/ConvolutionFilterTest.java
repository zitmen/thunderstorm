package ThunderSTORM.filters;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConvolutionFilterTest {
    
    /**
     * Test of updateKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testUpdateKernel_3args_1() {
        System.out.println("updateKernel");
        FloatProcessor kernel = null;
        boolean separable_kernel = false;
        int padding_method = 0;
        ConvolutionFilter instance = null;
        instance.updateKernel(kernel, separable_kernel, padding_method);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testUpdateKernel_3args_2() {
        System.out.println("updateKernel");
        FloatProcessor kernel_x = null;
        FloatProcessor kernel_y = null;
        int padding_method = 0;
        ConvolutionFilter instance = null;
        instance.updateKernel(kernel_x, kernel_y, padding_method);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of filterImage method, of class ConvolutionFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("filterImage");
        FloatProcessor image = null;
        ConvolutionFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.filterImage(image);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKernelX method, of class ConvolutionFilter.
     */
    @Test
    public void testGetKernelX() {
        System.out.println("getKernelX");
        ConvolutionFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.getKernelX();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKernelY method, of class ConvolutionFilter.
     */
    @Test
    public void testGetKernelY() {
        System.out.println("getKernelY");
        ConvolutionFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.getKernelY();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testGetKernel() {
        System.out.println("getKernel");
        ConvolutionFilter instance = null;
        FloatProcessor expResult = null;
        FloatProcessor result = instance.getKernel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}