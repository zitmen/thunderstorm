package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.util.Arrays;

public class UniformFilter extends ConvolutionFilter {

    protected static float[] getKernel(int size, float value) {
        float[] kernel = new float[size];
        Arrays.fill(kernel, value);
        return kernel;
    }
    
    protected int size;
    
    protected void updateKernel(int size, float value) {
        super.updateKernel(new FloatProcessor(1, size, getKernel(size, value)), true);
    }

    public UniformFilter(int size, float value) {
        super(new FloatProcessor(1, size, getKernel(size, value)), true, Padding.PADDING_DUPLICATE);
    }
    
    public UniformFilter(int size, float value, int padding_method) {
        super(new FloatProcessor(1, size, getKernel(size, value)), true, padding_method);
    }

}