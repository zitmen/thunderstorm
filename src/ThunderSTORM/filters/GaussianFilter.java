package ThunderSTORM.filters;

import static ThunderSTORM.utils.Math.gauss;
import ThunderSTORM.utils.Padding;
import ij.process.FloatProcessor;

public class GaussianFilter extends ConvolutionFilter {
    
    private static float [] getKernel(int size, double sigma)
    {
        float [] kernel = new float[size];
        for(int i = 0, center = size/2; i < size; i++) {
            kernel[i] = (float) gauss(i - center, sigma, true);
        }
        return kernel;
    }

    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, Padding.PADDING_DUPLICATE);
    }
    
}
