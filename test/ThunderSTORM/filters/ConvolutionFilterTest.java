package ThunderSTORM.filters;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class ConvolutionFilterTest {
    
    /**
     * Test of updateKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testUpdateKernel() {
        System.out.println("updateKernel");
        FloatProcessor kernel = null;
        boolean separable_kernel = false;
        int padding_method = 0;
        ConvolutionFilter instance = null;
        instance.updateKernel(kernel, separable_kernel, padding_method);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        // testnout, jestli budou funkce getKernel* vracet, co vracet maji (vcetne null)
    }

    /**
     * Test of updateKernel method, of class ConvolutionFilter.
     */
    @Test
    public void testUpdateKernelStrictlySeparable() {
        System.out.println("updateKernel");
        FloatProcessor kernel_x = null;
        FloatProcessor kernel_y = null;
        int padding_method = 0;
        ConvolutionFilter instance = null;
        instance.updateKernel(kernel_x, kernel_y, padding_method);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        // testnout, jestli budou funkce getKernel* vracet, co vracet maji (vcetne null)
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
        // testnout asi na nejakym peknym kernelu, jestli se vysledek ze separable rovna vysledku ze slozenyho
        // ...to uz neni potreba pak dal porovnavat, protoze tedty na konvoluci jako takovou uz mam v Convolution::convolve
    }

}