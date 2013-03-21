package ThunderSTORM.filters;

import ThunderSTORM.utils.Padding;
import ij.process.FloatProcessor;

public class WaveletFilter extends ConvolutionFilter {

    private static float [] getKernel(int plane) {
        switch(plane) {
            case 1: return new float[]{1f/16f, 1f/4f, 3f/8f, 1f/4f, 1f/16f};
            case 2: return new float[]{1f/16f, 0f, 1f/4f, 0f, 3f/8f, 0f, 1f/4f, 0f, 1f/16f};
            case 3: return new float[]{1f/16f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 3f/8f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 1f/16f};
            default: throw new UnsupportedOperationException("Only planes 1, 2, and 3 are currently supported!");
        }
    }
    
    public WaveletFilter(int plane) {
        super(new FloatProcessor(1, getKernel(plane).length, getKernel(plane)), true, Padding.PADDING_DUPLICATE);   // the `getKernel(plane).length` is very ugly and slow, but the `super()` has to be on first line!
    }
    
}
