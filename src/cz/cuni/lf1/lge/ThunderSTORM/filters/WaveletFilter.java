package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;

/**
 *
 */
public class WaveletFilter extends ConvolutionFilter {

    private static float [] getKernel(int plane) throws UnsupportedOperationException {
        switch(plane) {
            case 1: return new float[]{1f/16f, 1f/4f, 3f/8f, 1f/4f, 1f/16f};
            case 2: return new float[]{1f/16f, 0f, 1f/4f, 0f, 3f/8f, 0f, 1f/4f, 0f, 1f/16f};
            case 3: return new float[]{1f/16f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 3f/8f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 1f/16f};
            default: throw new UnsupportedOperationException("Only planes 1, 2, and 3 are currently supported!");
        }
    }
    
    /**
     *
     * @param plane
     * @throws UnsupportedOperationException
     */
    public WaveletFilter(int plane) throws UnsupportedOperationException {
        super(new FloatProcessor(1, getKernel(plane).length, getKernel(plane)), true, Padding.PADDING_DUPLICATE);   // the `getKernel(plane).length` is very ugly and slow, but the `super()` has to be on first line!
    }
    
    /**
     *
     * @param plane
     * @param padding_method
     * @throws UnsupportedOperationException
     */
    public WaveletFilter(int plane, int padding_method) throws UnsupportedOperationException {
        super(new FloatProcessor(1, getKernel(plane).length, getKernel(plane)), true, padding_method);   // the `getKernel(plane).length` is very ugly and slow, but the `super()` has to be on first line!
    }
    
}
