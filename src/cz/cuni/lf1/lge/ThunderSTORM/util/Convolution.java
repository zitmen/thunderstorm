package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.FloatProcessor;

/**
 * Convolution algorithm.
 */
public class Convolution {

    /**
     * 2D convolution of an input image with a specified kernel.
     * 
     * A two dimensional convolution:
     * {@mathjax (image*kernel)(s,t) = \int_{-\infty}^{+\infty}{\int_{-\infty}^{+\infty}{image(x,y) \cdot kernel(s-x,t-y) \; \mathrm{d}x\mathrm{d}y}}}
     *
     * @param image an input image
     * @param kernel convolution kernel
     * @param padding_type one of the padding types ({@code PADDING_NONE}, {@code PADDING_ZERO}, {@code PADDING_DUPLICATE}, or {@code PADDING_CYCLIC})
     * @return a <strong>new instance</strong> of FloatProcessor that contains the convolved image
     */
    public static FloatProcessor convolve2D(FloatProcessor image, FloatProcessor kernel, int padding_type) {
        assert kernel.getWidth() % 2 == 1;
        assert kernel.getHeight() % 2 == 1;

        int kw = kernel.getWidth(), kh = kernel.getHeight(), padsize = java.lang.Math.max(kw, kh) / 2;
        int iw = image.getWidth(), ih = image.getHeight(), idx;
        if(padding_type == Padding.PADDING_NONE) { iw -= 2*padsize; ih -= 2*padsize; }
        FloatProcessor img = (FloatProcessor) Padding.addBorder(image, padding_type, padsize);

        // convolution
        float[] result = new float[iw*ih];
        for (int ix = 0; ix < iw; ix++) {
            for (int iy = 0; iy < ih; iy++) {
                idx = iy * iw + ix;
                for (int kx = 0; kx < kw; kx++) {
                    for (int ky = 0; ky < kh; ky++) {
                        result[idx] += kernel.getf(kw-1-kx, kh-1-ky) * img.getf(padsize + ix + (kx - kw / 2), padsize + iy + (ky - kh / 2));
                        // Note: `kernel.getf(kw-1-kx, kh-1-ky)` is very important!! if the code would be just `kernel.getf(kx, ky)` then the
                        //       function would be failing when applied on asymetric kernels!
                        // --> from imfilter2 documentation: Two-dimensional correlation is equivalent to two-dimensional convolution with the filter matrix rotated 180 degrees.
                    }
                }
            }
        }

        return new FloatProcessor(iw, ih, result);
    }

}
