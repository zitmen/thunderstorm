package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.util.Arrays;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class UniformFilter extends ConvolutionFilter {

    /**
     *
     * @param size
     * @param value
     * @return
     */
    protected static float[] getKernel(int size, float value) {
        float[] kernel = new float[size];
        Arrays.fill(kernel, value);
        return kernel;
    }
    
    /**
     *
     */
    protected int size;
    
    /**
     *
     * @param size
     * @param value
     */
    protected void updateKernel(int size, float value) {
        super.updateKernel(new FloatProcessor(1, size, getKernel(size, value)), true);
    }

    /**
     *
     * @param size
     * @param value
     */
    public UniformFilter(int size, float value) {
        super(new FloatProcessor(1, size, getKernel(size, value)), true, Padding.PADDING_DUPLICATE);
    }
    
    /**
     *
     * @param size
     * @param value
     * @param padding_method
     */
    public UniformFilter(int size, float value, int padding_method) {
        super(new FloatProcessor(1, size, getKernel(size, value)), true, padding_method);
    }

}