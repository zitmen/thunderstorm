package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.Blitter;
import ij.process.FloatProcessor;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class Padding {
    
    /**
     *
     */
    public static final int PADDING_NONE = 0;
    /**
     *
     */
    public static final int PADDING_ZERO = 1;
    /**
     *
     */
    public static final int PADDING_DUPLICATE = 2;
    /**
     *
     */
    public static final int PADDING_CYCLIC = 3;

    // always returns a newly allocated image
    /**
     *
     * @param image
     * @param type
     * @param size
     * @return
     */
    public static FloatProcessor addBorder(FloatProcessor image, int type, int size) {
        assert size >= 0;
        assert type >= 0 && type <= 3;
        
        switch(type) {
            case PADDING_NONE: return paddingNone(image);
            case PADDING_ZERO: return paddingZero(image, size);
            case PADDING_DUPLICATE: return paddingDuplicate(image, size);
            case PADDING_CYCLIC: return paddingCyclic(image, size);
            default: throw new UnsupportedOperationException("Unsupported padding method!");
        }
    }
    
    private static FloatProcessor paddingNone(FloatProcessor image) {
        FloatProcessor out = new FloatProcessor(image.getWidth(), image.getHeight());
        out.copyBits(image, 0, 0, Blitter.COPY);
        return out;
    }
    
    private static FloatProcessor paddingZero(FloatProcessor image, int size) {
        FloatProcessor out = new FloatProcessor(image.getWidth() + 2*size, image.getHeight() + 2*size);
        // fill the output image with zeros
        out.setValue(0);
        out.fill();
        // finally, insert the input image inside the output image
        out.copyBits(image, size, size, Blitter.COPY);
        return out;
    }
    
    private static FloatProcessor paddingDuplicate(FloatProcessor image, int size) {
        int w = image.getWidth();
        int h = image.getHeight();
        int ow = w + 2 * size;
        int oh = h + 2 * size;
        
        FloatProcessor out = new FloatProcessor(ow, oh);
        out.copyBits(image, size, size, Blitter.COPY);  // put the original image into the center
        
        // top left corner of border
        out.setRoi(0, 0, size, size);
        out.setValue(image.getf(0, 0));
        out.fill();
        
        // top right corner of border
        out.setRoi(size+w, 0, size, size);
        out.setValue(image.getf(w-1, 0));
        out.fill();
        
        // bottom left corner of border
        out.setRoi(0, size+h, size, size);
        out.setValue(image.getf(0, h-1));
        out.fill();
        
        // bottom right corner of border
        out.setRoi(size+w, size+h, size, size);
        out.setValue(image.getf(w-1, h-1));
        out.fill();
        
        // horizontal borders
        for(int y = 0; y < size; y++) {
            for(int x = 0; x < w; x++) {
                out.setf(x+size, y, image.getf(x, 0));
                out.setf(x+size, oh-1-y, image.getf(x, h-1));
            }
        }
        
        // vertical borders
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < h; y++) {
                out.setf(x, y+size, image.getf(0, y));
                out.setf(ow-1-x, y+size, image.getf(w-1, y));
            }
        }
        
        return out;
    }
    
    private static FloatProcessor paddingCyclic(FloatProcessor image, int size) {
        int w = image.getWidth();
        int h = image.getHeight();
        int ow = w + 2 * size;
        int oh = h + 2 * size;
        
        FloatProcessor out = new FloatProcessor(ow,oh);
        for(int ox = 0, x = w-(size%w); ox < ow; x++, ox++) {
            for(int oy = 0, y = h-(size%h); oy < oh; y++, oy++) {
                out.setf(ox, oy, image.getf(x%w,y%h));
            }
        }
        
        return out;
    }
    
}
