package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;

public class LoweredGaussianFilter extends ConvolutionFilter {
    
    private static float [] getKernel(int size, double sigma, boolean truncate)
    {
        float [] kernel = new float[size*size];
        // TODO: generate lowered (truncated) Gaussian
        return kernel;
    }
    
    public LoweredGaussianFilter(int size, double sigma, boolean truncate) {
        super(new FloatProcessor(size, size, getKernel(size, sigma, truncate)), Convolution.PADDING_DUPLICATE);
    }
    
}
