package ThunderSTORM.utils;

import static java.lang.Math.max;
import ij.process.FloatProcessor;
import java.util.Arrays;

public class Morphology {
    
    public static FloatProcessor dilate(FloatProcessor image, FloatProcessor kernel) {
        float val;
        int xc = kernel.getWidth() / 2, yc = kernel.getHeight()/ 2;
        FloatProcessor img = Padding.addBorder(image, max(xc, yc), Padding.PADDING_DUPLICATE);
        FloatProcessor out = (FloatProcessor) image.createProcessor(image.getWidth(), image.getHeight());
        for(int x = xc, xm = xc+image.getWidth(); x < xm; x++) {
            for(int y = yc, ym = yc+image.getHeight(); y < ym; y++) {
                for(int i = 0, im = kernel.getWidth(); i < im; i++) {
                    for(int j = 0, jm = kernel.getHeight(); j < jm; j++) {
                        val = kernel.getPixelValue(i, j) * img.getPixelValue(x+(i-xc), y+(j-yc));
                        if(val > out.getPixelValue(i, j)) {
                            out.setf(x-xc, y-yc, val);
                        }
                    }
                }
            }
        }
        return out;
    }

    public static FloatProcessor dilateBox(FloatProcessor image, int radius) {
        return dilate(image, createBoxKernel(radius));
    }
    
    private static FloatProcessor createBoxKernel(int radius) {
        float [] pixels = new float[radius*radius];
        Arrays.fill(pixels, 1.0f);
        return new FloatProcessor(radius, radius, pixels);
    }
    
}
