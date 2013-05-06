package cz.cuni.lf1.lge.ThunderSTORM.util;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
import ij.process.FloatProcessor;
import java.util.Arrays;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class Morphology {
    
    /**
     *
     * @param image
     * @param kernel
     * @return
     */
    public static FloatProcessor dilate(FloatProcessor image, FloatProcessor kernel) {
        float val;
        int xc = kernel.getWidth() / 2, yc = kernel.getHeight()/ 2;
        FloatProcessor img = Padding.addBorder(image, Padding.PADDING_DUPLICATE, max(xc, yc));
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
     *
     * @param image
     * @param radius
     * @return
     */
    public static FloatProcessor dilateBox(FloatProcessor image, int radius) {
        return dilate(image, createBoxKernel(radius));
    }
    
    private static FloatProcessor createBoxKernel(int radius) {
        float [] pixels = new float[radius*radius];
        Arrays.fill(pixels, 1.0f);
        return new FloatProcessor(radius, radius, pixels);
    }
    
}
