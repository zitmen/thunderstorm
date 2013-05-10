package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Convolution;
import ij.process.FloatProcessor;

/**
 *
 */
public class ConvolutionFilter implements IFilter {

    private int padding_method;
    private FloatProcessor kernel = null, kernel_x = null, kernel_y = null;
    
    /**
     *
     * @param padding_method
     */
    public final void updatePaddingMethod(int padding_method) {
        this.padding_method = padding_method;
    }

    /**
     *
     * @param kernel
     * @param separable_kernel
     */
    public final void updateKernel(FloatProcessor kernel, boolean separable_kernel) {
        if (separable_kernel) {
            this.kernel = null;
            if (kernel.getWidth() > 1) {    // kernel = kernel_x -> to get kernel_y (transposition) it has to be rotated to right
                this.kernel_x = kernel;
                this.kernel_y = (FloatProcessor) kernel.rotateRight();
            } else {                        // kernel = kernel_y -> to get kernel_x (transposition) it has to be rotated to left
                this.kernel_x = (FloatProcessor) kernel.rotateLeft();
                this.kernel_y = kernel;
            }
        } else {
            this.kernel = kernel;
            this.kernel_x = null;
            this.kernel_y = null;
        }
    }
    
    /**
     *
     * @param kernel_x
     * @param kernel_y
     */
    public final void updateKernel(FloatProcessor kernel_x, FloatProcessor kernel_y) {
        this.kernel = null;
        this.kernel_x = kernel_x;
        this.kernel_y = kernel_y;
    }
    
    /**
     *
     * @param kernel
     * @param separable_kernel
     * @param padding_method
     */
    public ConvolutionFilter(FloatProcessor kernel, boolean separable_kernel, int padding_method) {
        updateKernel(kernel, separable_kernel);
        updatePaddingMethod(padding_method);
    }
    
    /**
     *
     * @param kernel_x
     * @param kernel_y
     * @param padding_method
     */
    public ConvolutionFilter(FloatProcessor kernel_x, FloatProcessor kernel_y, int padding_method) {
        updateKernel(kernel_x, kernel_y);
        updatePaddingMethod(padding_method);
    }

    /**
     *
     * @param image
     * @return
     */
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        // With non-separable kernels, the complexity is K*K*N,
        if (kernel != null) return Convolution.convolve2D(image, kernel, padding_method);
        // while with separable kernels of length K, the computational complexity is 2*K*N, where N is number of pixels of the image!
        return Convolution.convolve2D(Convolution.convolve2D(image, kernel_y, padding_method), kernel_x, padding_method);
    }
    
    /**
     *
     * @return
     */
    public FloatProcessor getKernelX(){
        return kernel_x;
    }
    
    /**
     *
     * @return
     */
    public FloatProcessor getKernelY(){
        return kernel_y;
    }
    
    /**
     *
     * @return
     */
    public FloatProcessor getKernel(){
        return kernel;
    }
            
}
