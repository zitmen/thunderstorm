package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.Blitter;
import ij.process.FloatProcessor;

/**
 * Padding of images, which is very useful for convolution and other image filters.
 */
public class Padding {
    
    /**
     * No padding at all - size of an output image is the same as the size of an input image.
     */
    public static final int PADDING_NONE = 0;
    
    /**
     * An input image is padded image with zeros.
     */
    public static final int PADDING_ZERO = 1;
    
    /**
     * An input image if padded with the values closest to the border.
     * For example the image
     * <pre>
     * {@code
     * 123
     * 456
     * 789}
     * </pre>
     * will be padded as follows:
     * <pre>
     * {@code
     * 11233
     * 11233
     * 44566
     * 77899
     * 77899}
     * </pre>
     */
    public static final int PADDING_DUPLICATE = 2;
    
    /**
     * Circular padding of an input image. The image repeat itself in the border.
     * For example the image
     * <pre>
     * {@code
     * 123
     * 456
     * 789}
     * </pre>
     * will be padded as follows:
     * <pre>
     * {@code
     * 94567
     * 31231
     * 64564
     * 97897
     * 31231}
     * </pre>
     */
    public static final int PADDING_CYCLIC = 3;

    /**
     * Method for padding an input image.
     *
     * @param image an input image
     * @param type a padding method ({@code PADDING_NONE}, {@code PADDING_ZERO}, {@code PADDING_DUPLICATE}, or {@code PADDING_CYCLIC})
     * @param size border width
     * @throws UnsupportedOperationException if {@code type} is neither of the types listed above
     * @return a <strong>new instance</strong> of FloatProcessor containing the padded input image
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
