package ThunderSTORM.utils;

import ij.process.FloatProcessor;
import java.util.Arrays;

public class Morphology {
    
    public static FloatProcessor dilate(FloatProcessor image, FloatProcessor kernel) {
        float val;
        int xc = kernel.getWidth() / 2, yc = kernel.getHeight()/ 2;
        FloatProcessor out = (FloatProcessor) image.createProcessor(image.getWidth(), image.getHeight());
        for(int i = 0, im = kernel.getWidth(); i < im; i++) {
            for(int j = 0, jm = kernel.getHeight(); j < jm; j++) {
                val = kernel.getPixelValue(i, j) * image.getPixelValue(i-xc, j-yc);
                if(val > out.getPixelValue(i, j)) {
                    out.setf(i, j, val);
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
