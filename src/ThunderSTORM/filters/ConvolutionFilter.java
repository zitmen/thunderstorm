package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;

public abstract class ConvolutionFilter implements IFilter {
    
    private int padding_method;
    private FloatProcessor kernel;
    
    public ConvolutionFilter(FloatProcessor kernel, int padding_method) {
        this.kernel = kernel;
        this.padding_method = padding_method;
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return Convolution.Convolve(image, kernel, padding_method);
    }
    
}
