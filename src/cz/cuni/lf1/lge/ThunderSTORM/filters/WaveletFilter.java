package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;

/**
 * This wavelet filter is a convolution filter with a separable kernel determined by B-splines.
 * 
 * This filter allows you to select one of three planes, each corresponding to different bandwidth.
 * <ol>
 *     <li>The first plane contains majority of high frequencies present in the input image including noise.</li>
 *     <li>In the second plane the noise is suppressed and the contains the structures with size close to the diffraction limit.</li>
 *     <li>And the third plane is just a low frequency image.</li>
 * </ol>
 * 
 * Note that each of the planes is more suitable for different operations. For example the first plane is used for
 * threshold estimation for a detector. The second plane, or less often the third plane, can be used as an input image
 * for a detector.
 * 
 * The undecimated wavelet transform is implemented as a convolution filter. The kernel is separable.
 * 
 * @see CompoundWaveletFilter
 * @see ConvolutionFilter
 */
public class WaveletFilter extends ConvolutionFilter {

    // Pre-defined kernels for different wavelet planes.
    static final float[][] kernels = {
        {1f/16f,             1f/4f,             3f/8f,             1f/4f,             1f/16f},
        {1f/16f,     0f,     1f/4f,     0f,     3f/8f,     0f,     1f/4f,     0f,     1f/16f},
        {1f/16f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 3f/8f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 1f/16f}};
    
    /**
     * Initialize the filter with a kernel corresponding to a specific plane.
     *
     * @param plane specifies kernel for what plane the filter will be initialized
     * 
     * @throws IndexOutOfBoundsException if the requested plane is not in range of supported plane, i.e., it is not in range from 1 to 3
     */
    public WaveletFilter(int plane) throws IndexOutOfBoundsException {
        super(new FloatProcessor(1, kernels[plane-1].length, kernels[plane-1]), true, Padding.PADDING_DUPLICATE);
    }
    
    /**
     * Initialize the filter with a kernel corresponding to a specific plane and select a padding method.
     *
     * @param plane specifies kernel for what plane the filter will be initialized
     * @param padding_method a padding method
     * 
     * @throws UnsupportedOperationException if the requested plane is not in range of supported plane, i.e., it is not in range from 1 to 3
     * 
     * @see Padding
     */
    public WaveletFilter(int plane, int padding_method) throws UnsupportedOperationException {
        super(new FloatProcessor(1, kernels[plane-1].length, kernels[plane-1]), true, padding_method);
    }
    
}
