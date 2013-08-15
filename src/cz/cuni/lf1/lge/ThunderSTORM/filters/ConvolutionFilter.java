package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Convolution;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import java.util.HashMap;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.crop;

/**
 * General convolution filter that performs 2D convolution of an input image with
 * a specified kernel.
 * 
 * The trick here is that it is possible to specify kernel either as 2D matrix,
 * or one or two 1D vectors. Thus if a kernel is separable, then you should
 * factorize your kernel from a matrix to a vector (in case of symmetric kernel)
 * or two vectors (in case of asymmetric kernel). This inevitably leads to reducing
 * the computational complexity from {@mathjax (W_i \cdot H_i) \cdot (W_k \cdot H_k)}
 * to {@mathjax (W_i \cdot H_i) \cdot (W_k + H_k)}, given that {@mathjax W} stands for width,
 * {@mathjax H} stands for height, and subscripts {@mathjax i,k} stand for image and kernel,
 * respectively.
 */
public class ConvolutionFilter {

    private int padding_method;
    private FloatProcessor kernel = null, kernel_x = null, kernel_y = null;
    
    protected FloatProcessor input = null, result = null;
    protected HashMap<String,FloatProcessor> export_variables = null;
    
    /**
     * Change the current padding method.
     * 
     * @param padding_method a padding method
     * 
     * @see Padding
     */
    public final void updatePaddingMethod(int padding_method) {
        this.padding_method = padding_method;
    }

    /**
     * Replace the current kernel by a new one.
     * 
     * The desciption of this method is identical with the description of contructor
     * {@code ConvolutionFilter(FloatProcessor, boolean, int)} with exception of the padding argument.
     *
     * @param kernel
     * @param separable_kernel
     */
    protected final void updateKernel(FloatProcessor kernel, boolean separable_kernel) {
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
     * Replace the current kernel by a new one.
     * 
     * The desciption of this method is identical with the description of contructor
     * {@code ConvolutionFilter(FloatProcessor, FloatProcessor, int)} with exception of the padding argument.
     *
     * @param kernel_x
     * @param kernel_y
     */
    protected final void updateKernel(FloatProcessor kernel_x, FloatProcessor kernel_y) {
        this.kernel = null;
        this.kernel_x = kernel_x;
        this.kernel_y = kernel_y;
    }
    
    /**
     * Initialize the filter.
     * 
     * Create either a non-separable kernel from a 2D {@code kernel} matrix, or separable
     * symmetric kernel from a single vector. And also set a padding method.
     *
     * @param kernel if {@code separable_kernel} is true, then 2D kernel matrix, or a 1D vector otherwise (can be row or column)
     * @param separable_kernel is the kernel entered in as separable?
     * @param padding_method a padding method
     * 
     * @see Padding
     */
    public ConvolutionFilter(FloatProcessor kernel, boolean separable_kernel, int padding_method) {
        updateKernel(kernel, separable_kernel);
        updatePaddingMethod(padding_method);
    }
    
    /**
     * Initialize the filter.
     * 
     * Create a separable kernel using X and Y component vectors and specify a padding method.
     * This method is usually used for specifying assymetric kernels, which have different
     * X and Y components. For other types of kernels you can call
     * {@code ConvolutionFilter(FloatProcessor, boolean, int)} signature instead.
     *
     * @param kernel_x X component of a separable kernel (must be a row vector)
     * @param kernel_y Y component of a separable kernel (must be a column vector)
     * @param padding_method a padding method
     * 
     * @see Padding
     */
    public ConvolutionFilter(FloatProcessor kernel_x, FloatProcessor kernel_y, int padding_method) {
        updateKernel(kernel_x, kernel_y);
        updatePaddingMethod(padding_method);
        export_variables = null;
    }

    public FloatProcessor filterImage(FloatProcessor image) {
        input = image;
        int padsize = (kernel!= null ? Math.max(kernel.getWidth(), kernel.getHeight()) : kernel_x.getPixelCount()) /2;
        if(padding_method == Padding.PADDING_NONE){
            padsize = 0;
        }
        FloatProcessor fp = Padding.addBorder(image, padding_method, padsize);
        Convolver ijConvolver = new Convolver();
        // With non-separable kernels, the complexity is K*K*N,
        if (kernel != null) {
            ijConvolver.convolve(fp, (float[])kernel.getPixels(), kernel.getWidth(), kernel.getHeight());
            result = crop(fp, padsize,padsize,image.getWidth(), image.getHeight());
            return result;
        }
        // while with separable kernels of length K, the computational complexity is 2*K*N, where N is number of pixels of the image!
        ijConvolver.convolve(fp, (float[])kernel_x.getPixels(), kernel_x.getWidth(), kernel_x.getHeight());
        ijConvolver.convolve(fp, (float[])kernel_y.getPixels(), kernel_y.getWidth(), kernel_y.getHeight());
        result = crop(fp, padsize,padsize,image.getWidth(), image.getHeight());
        return result;
    }
    
    /**
     * Return a row vector, which is part of a separable kernel or return null if the kernel is not separable.
     *
     * @return a row vector, which is part of a separable kernel or return null if the kernel is not separable
     */
    public FloatProcessor getKernelX(){
        return kernel_x;
    }
    
    /**
     * Return a column vector, which is part of a separable kernel or return null if the kernel is not separable.
     *
     * @return a column vector, which is part of a separable kernel or return null if the kernel is not separable
     */
    public FloatProcessor getKernelY(){
        return kernel_y;
    }
    
    /**
     * Return a 2D kernel matrix, or return null if the kernel is separable.
     *
     * @return a 2D kernel matrix, or return null if the kernel is separable
     */
    public FloatProcessor getKernel(){
        return kernel;
    }
            
}
