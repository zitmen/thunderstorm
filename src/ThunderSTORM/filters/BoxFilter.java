package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;
import java.util.Arrays;

public class BoxFilter extends ConvolutionFilter {
    
    private static float [] getKernel(int size)
    {
        float [] kernel = new float[size*size];
        Arrays.fill(kernel, 1.0f / (float) kernel.length);
        return kernel;
    }
    
    public BoxFilter(int size) {
        super(new FloatProcessor(size, size, getKernel(size)), Convolution.PADDING_DUPLICATE);
    }
    
}
