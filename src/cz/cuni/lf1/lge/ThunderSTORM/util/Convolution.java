package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.FloatProcessor;

/**
 * Convolution algorithm.
 */
public class Convolution {

    /**
     * 2D convolution of an input image with a specified kernel.
     *
     * A two dimensional convolution: {
     *
     * @mathjax (image*kernel)(s,t) =
     * \int_{-\infty}^{+\infty}{\int_{-\infty}^{+\infty}{image(x,y) \cdot
     * kernel(s-x,t-y) \; \mathrm{d}x\mathrm{d}y}}}
     *
     * @param image an input image
     * @param kernel convolution kernel
     * @param padding_type one of the padding types
     * ({@code PADDING_NONE}, {@code PADDING_ZERO}, {@code PADDING_DUPLICATE},
     * or {@code PADDING_CYCLIC})
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     * the convolved image
     */
    public static FloatProcessor convolve2D(FloatProcessor image, FloatProcessor kernel, int padding_type) {
        assert kernel.getWidth() % 2 == 1;
        assert kernel.getHeight() % 2 == 1;

        int kw = kernel.getWidth(), kh = kernel.getHeight(), padsize = java.lang.Math.max(kw, kh) / 2;
        int iw = image.getWidth(), ih = image.getHeight(), idx;
        //if(padding_type == Padding.PADDING_NONE) { iw -= 2*padsize; ih -= 2*padsize; }
        FloatProcessor img = (FloatProcessor) Padding.addBorder(image, padding_type, padsize);

        // convolution
        float[] result = new float[iw * ih];

        float[] kernelPixels = (float[]) kernel.getPixels();
        int width = img.getWidth();
        int height = img.getHeight();
        int x1 = padsize;
        int y1 = padsize;
        int x2 = width - padsize;
        int y2 = height - padsize;
        int uc = kw / 2;
        int vc = kh / 2;
        float[] pixels = result;
        float[] pixels2 = (float[]) img.getPixels();
        double sum;
        int offset, i;
        idx = padding_type == Padding.PADDING_NONE ? iw * padsize + x1 : 0;
        for(int y = y1; y < y2; y++) {
            for(int x = x1; x < x2; x++) {
                sum = 0.0;
                i = 0;
                for(int v = -vc; v <= vc; v++) {
                    offset = x + (y - v) * width;
                    for(int u = -uc; u <= uc; u++) {
                        sum += pixels2[offset - u] * kernelPixels[i++];
                    }
                }
                pixels[idx++] = (float) (sum);
            }
            if(padding_type == Padding.PADDING_NONE){
                idx += 2*padsize;
            }
        }


        return new FloatProcessor(iw, ih, pixels);
    }
}
