package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;

public class GaussianFilter extends ConvolutionFilter {
    
    private static float [] getKernel(int size, double sigma)
    {
        float [] kernel = new float[size*size];
        // TODO: generate Gaussian
        return kernel;
    }
    
    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(size, size, getKernel(size, sigma)), Convolution.PADDING_DUPLICATE);
    }
    
}
