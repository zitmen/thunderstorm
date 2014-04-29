package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.util.Arrays;

/**
 * Uniform filter is a convolution filter with its kernel matrix filled with
 * only one constant value (a perfect example of this is a box filter).
 * 
 * This filter uses the separable kernel feature.
 * 
 * @see ConvolutionFilter
 */
public class UniformFilter extends ConvolutionFilter {

    /**
     * Generate a new square kernel of specified size and filled with a specified value.
     *
     * @param size size of the kernel
     * @param value value you want the kernel fill with
     * @return a new 2D square array of specified size and filled with a specified value
     */
    protected static float[] getKernel(int size, float value) {
        float[] kernel = new float[size];
        Arrays.fill(kernel, value);
        return kernel;
    }
    
    /**
     * Size of the currently loaded kernel.
     */
    protected int size;
    
    /**
     * Replace the currently loaded kernel with a new kernel of specified size and filled with a specified value.
     *
     * @param size size of the kernel
     * @param value value you want the kernel fill with
     */
    protected void updateKernel(int size, float value) {
        super.updateKernel(new FloatProcessor(1, size, getKernel(size, value), null), true);
    }

    /**
     * Initialize the filter.
     * 
     * Padding is by default set to {@code PADDING_DUPLICATE} option.
     *
     * @param size size of the kernel
     * @param value value you want the kernel fill with
     * 
     * @see Padding
     */
    public UniformFilter(int size, float value) {
        super(new FloatProcessor(1, size, getKernel(size, value), null), true, Padding.PADDING_DUPLICATE);
    }
    
    /**
     * Initialize the filter.
     * 
     * @param size size of the kernel
     * @param value value you want the kernel fill with
     * @param padding_method a padding method
     * 
     * @see Padding
     */
    public UniformFilter(int size, float value, int padding_method) {
        super(new FloatProcessor(1, size, getKernel(size, value), null), true, padding_method);
    }

}