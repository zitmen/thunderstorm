package cz.cuni.lf1.lge.ThunderSTORM.util;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.max;
import ij.process.FloatProcessor;
import java.util.Arrays;

/**
 * Morphological operations (only dilation was needed so far).
 */
public class Morphology {
    
    /**
     * Grayscale dilation of an input {@code image} by a specified {@code kernel}.
     * <ol>
     *   <li>pad the input image with {@code PADDING_DUPLICATE} method and with half size of a maximum of width or height</li>
     *   <li>
     *      apply the grayscale dilation with the specified {@code kernel} on the input {@code image} given by
     *      {@mathjax (image \oplus kernel)(x,y) = \sup_{i,j \in K}[image\_padded(x-i,y-j) \cdot kernel(i,j)]}, where {@mathjax \sup} stands
     *      for <a href="http://en.wikipedia.org/wiki/Supremum">supremum</a>, {@mathjax x,y} are pixel coordinates in the
     *      {@mathjax image}, and {@mathjax i,j} are coordinates in the {@mathjax kernel}. Note that {@mathjax \oplus} is an operator of
     *      grayscale dilation and {@mathjax \cdot} is an operator of multiplication.
     *   </li>
     * </ol>
     *
     * @see Padding
     * @param image input image
     * @param kernel dilation kernel
     * @return a new instance of FloatProcessor which contains the input {@code image} after dilation with {@code kernel}
     */
    public static FloatProcessor dilate(FloatProcessor image, FloatProcessor kernel) {
        float val;
        int xc = kernel.getWidth() / 2, yc = kernel.getHeight()/ 2;
        FloatProcessor img = Padding.addBorder(image, Padding.PADDING_ZERO, max(xc, yc));
        FloatProcessor out = (FloatProcessor) image.createProcessor(image.getWidth(), image.getHeight());
        for(int x = xc, xm = xc+image.getWidth(); x < xm; x++) {
            for(int y = yc, ym = yc+image.getHeight(); y < ym; y++) {
                for(int i = 0, im = kernel.getWidth(); i < im; i++) {
                    for(int j = 0, jm = kernel.getHeight(); j < jm; j++) {
                        val = kernel.getf(i, j) * img.getf(x+(i-xc), y+(j-yc));
                        if(val > out.getf(x-xc,y-yc))
                            out.setf(x-xc, y-yc, val);
                    }
                }
            }
        }
        return out;
    }

    /**
     * Grayscale dilation of an input {@code image} by a box kernel with the specified {@code width}.
     * Box kernel is a square matrix filled with ones.
     *
     * @see Padding
     * @param image input image
     * @param width width of the box kernel
     * @return a new instance of FloatProcessor which contains the input {@code image} after dilation with {@code kernel}
     */
    public static FloatProcessor dilateBox(FloatProcessor image, int width) {
        return dilate(image, createBoxKernel(width));
    }
    
    private static FloatProcessor createBoxKernel(int width) {
        float [] pixels = new float[width*width];
        Arrays.fill(pixels, 1.0f);
        return new FloatProcessor(width, width, pixels, null);
    }
    
}
