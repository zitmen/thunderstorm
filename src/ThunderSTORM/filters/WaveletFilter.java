package ThunderSTORM.filters;

import ThunderSTORM.utils.Convolution;
import ij.process.FloatProcessor;

public class WaveletFilter extends ConvolutionFilter {
    
    private static float [] getKernel(int level)
    {
        // TODO: generate Wavelet kernel
        return null;
    }
    
    public WaveletFilter(int level) {
        super(new FloatProcessor(1, 1, getKernel(level)), Convolution.PADDING_DUPLICATE);
    }
    
}
