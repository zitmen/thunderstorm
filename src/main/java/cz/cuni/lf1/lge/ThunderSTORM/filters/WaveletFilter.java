package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.BSplines;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.pow;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.util.Arrays;

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

    /**
     * Initialize the filter with a kernel corresponding to a specific plane.
     *
     * @param plane specifies kernel for what plane the filter will be initialized
     * 
     * @throws IndexOutOfBoundsException if the requested plane is not in range of supported plane, i.e., it is not in range from 1 to 3
     */
    public WaveletFilter(int plane, int spline_order, double spline_scale, int n_samples) throws IndexOutOfBoundsException {
        double [] kernel = getKernel(plane, spline_order, spline_scale, n_samples);
        updateKernel(new FloatProcessor(1, kernel.length, kernel), true);
        updatePaddingMethod(Padding.PADDING_DUPLICATE);
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
    public WaveletFilter(int plane, int spline_order, double spline_scale, int n_samples, int padding_method) throws UnsupportedOperationException {
        double [] kernel = getKernel(plane, spline_order, spline_scale, n_samples);
        updateKernel(new FloatProcessor(1, kernel.length, kernel), true);
        updatePaddingMethod(padding_method);
    }

    private double [] getKernel(int plane, int spline_order, double spline_scale, int n_samples) {
        double [] samples = new double[n_samples];
        for(int i = 0; i < n_samples; i++) {
            samples[i] = i - n_samples / 2;
        }
        double [] spline = BSplines.bSplineBlender(spline_order, spline_scale, samples);
        if(plane == 1) {
            return spline;
        } else {
            int step = (int)pow(2, plane-1);
            int n = (step * (n_samples - 1)) + 1;
            double [] kernel = new double[n];
            Arrays.fill(kernel, 0.0);
            for(int i = 0; i < spline.length; i++) {
                kernel[i*step] = spline[i];
            }
            return kernel;
        }
    }
}
