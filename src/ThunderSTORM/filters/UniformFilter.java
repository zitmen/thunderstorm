package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;
import java.util.Arrays;

public class UniformFilter extends ConvolutionFilter {

    protected static float[] getKernel(int size, float value) {
        float[] kernel = new float[size];
        Arrays.fill(kernel, value);
        return kernel;
    }

    public UniformFilter(int size, float mean) {
        super(new FloatProcessor(1, size, getKernel(size, mean)), true, Convolution.PADDING_DUPLICATE);
    }

}