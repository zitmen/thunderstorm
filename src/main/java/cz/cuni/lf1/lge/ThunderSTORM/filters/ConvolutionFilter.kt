package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.util.Convolution
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding
import ij.process.FloatProcessor

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
class ConvolutionFilter private constructor(
        val kernel: FloatProcessor?,
        val kernelX: FloatProcessor?,
        val kernelY: FloatProcessor?,
        val paddingMethod: Padding) {

    fun filterImage(image: FloatProcessor): FloatProcessor {
        if (kernel != null) {
            // With non-separable kernels, the complexity is K*K*N,
            return Convolution.convolve2D(image, kernel, paddingMethod);
        } else {
            // while with separable kernels of length K, the computational complexity is 2*K*N, where N is number of pixels of the image!
            return Convolution.convolve2D(Convolution.convolve2D(image, kernelY!!, paddingMethod), kernelX!!, paddingMethod);
        }
    }

    companion object {
        fun createFromKernel(kernel: FloatProcessor, paddingMethod: Padding): ConvolutionFilter {
            return ConvolutionFilter(kernel, null, null, paddingMethod)
        }

        fun createFromSeparableKernel(kernelX: FloatProcessor, kernelY: FloatProcessor, paddingMethod: Padding): ConvolutionFilter {
            return ConvolutionFilter(null, kernelX, kernelY, paddingMethod)
        }

        fun createFromSeparableKernel(kernel: FloatProcessor, paddingMethod: Padding): ConvolutionFilter {
            if (kernel.width > 1 && kernel.height > 1) {
                throw IllegalArgumentException("Separable kernel must be created from column or row vector!")
            }
            if (kernel.width > 1) {
                // kernel = kernel_x -> to get kernel_y (transposition) it has to be rotated to right
                return ConvolutionFilter(null, kernel, kernel.rotateRight() as FloatProcessor, paddingMethod)
            } else {
                // kernel = kernel_y -> to get kernel_x (transposition) it has to be rotated to left
                return ConvolutionFilter(null, kernel.rotateLeft() as FloatProcessor, kernel, paddingMethod)
            }
        }
    }
}
