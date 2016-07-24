package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.Blitter;
import ij.process.FloatProcessor;

/**
 * Padding of images, which is very useful for convolution and other image filters.
 */
enum class Padding {
    
    /**
     * No padding at all - size of an output image is the same as the size of an input image.
     */
    PADDING_NONE,
    
    /**
     * An input image is padded image with zeros.
     */
    PADDING_ZERO,
    
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
    PADDING_DUPLICATE,
    
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
    PADDING_CYCLIC;

    /**
     * Method for padding an input image.
     *
     * @param image an input image
     * @param type a padding method ({@code PADDING_NONE}, {@code PADDING_ZERO}, {@code PADDING_DUPLICATE}, or {@code PADDING_CYCLIC})
     * @param size border width
     * @throws UnsupportedOperationException if {@code type} is neither of the types listed above
     * @return a <strong>new instance</strong> of FloatProcessor containing the padded input image
     */
    fun addBorder(image: FloatProcessor, size: Int): FloatProcessor {
        if (size < 0) {
            throw IllegalArgumentException("Size must be >= 0!")
        }

        when (this) {
            PADDING_NONE -> return paddingNone(image)
            PADDING_ZERO -> return paddingZero(image, size)
            PADDING_DUPLICATE -> return paddingDuplicate(image, size)
            PADDING_CYCLIC -> return paddingCyclic(image, size)
            else -> throw UnsupportedOperationException("Unsupported padding method!")
        }
    }
    
    /**
     * Only supports padding with zero now
     */
    fun padToBiggerSquare(image: FloatProcessor, targetImageSize: Int): FloatProcessor{
        if (targetImageSize < image.width || targetImageSize < image.height) {
            throw IllegalArgumentException("Target image size must not be smaller then current image size!")
        }

        when (this) {
            PADDING_NONE -> return paddingNone(image)
            PADDING_ZERO -> return paddingZeroSquare(image, targetImageSize)
            else -> throw UnsupportedOperationException("Unsupported padding method!")
        }
    }
    
    private fun paddingNone(image: FloatProcessor): FloatProcessor {
        val out = FloatProcessor(image.width, image.height)
        out.copyBits(image, 0, 0, Blitter.COPY);
        return out;
    }
    
    private fun paddingZeroSquare(image: FloatProcessor, targetImageSize: Int): FloatProcessor {
        val out = FloatProcessor(targetImageSize,targetImageSize)
        // fill the output image with zeros
        out.setValue(0.0)
        out.fill()
        // finally, insert the input image inside the output image
        val xloc = (targetImageSize-image.getWidth())/2
        val yloc = (targetImageSize-image.getHeight())/2
        out.copyBits(image, xloc, yloc, Blitter.COPY)
        return out
    }
    
    private fun paddingZero(image: FloatProcessor, size: Int): FloatProcessor {
        val out = FloatProcessor(image.width + 2*size, image.height + 2*size)
        // fill the output image with zeros
        out.setValue(0.0)
        out.fill()
        // finally, insert the input image inside the output image
        out.copyBits(image, size, size, Blitter.COPY)
        return out
    }
    
    private fun paddingDuplicate(image: FloatProcessor, size: Int): FloatProcessor {
        val w = image.width
        val h = image.height
        val ow = w + 2 * size
        val oh = h + 2 * size
        
        val out = FloatProcessor(ow, oh)
        out.copyBits(image, size, size, Blitter.COPY)  // put the original image into the center
        
        // top left corner of border
        out.setRoi(0, 0, size, size)
        out.setValue(image.getf(0, 0).toDouble())
        out.fill()
        
        // top right corner of border
        out.setRoi(size+w, 0, size, size)
        out.setValue(image.getf(w-1, 0).toDouble())
        out.fill()
        
        // bottom left corner of border
        out.setRoi(0, size+h, size, size)
        out.setValue(image.getf(0, h-1).toDouble())
        out.fill()
        
        // bottom right corner of border
        out.setRoi(size+w, size+h, size, size)
        out.setValue(image.getf(w-1, h-1).toDouble())
        out.fill()
        
        // horizontal borders
        for(y in 0..(size-1)) {
            for(x in 0..(w-1)) {
                out.setf(x+size, y, image.getf(x, 0))
                out.setf(x+size, oh-1-y, image.getf(x, h-1))
            }
        }
        
        // vertical borders
        for(x in 0..(size-1)) {
            for(y in 0..(h-1)) {
                out.setf(x, y+size, image.getf(0, y))
                out.setf(ow-1-x, y+size, image.getf(w-1, y))
            }
        }
        
        return out
    }
    
    private fun paddingCyclic(image: FloatProcessor, size: Int): FloatProcessor {
        val w = image.width
        val h = image.height
        val ow = w + 2 * size
        val oh = h + 2 * size

        val out = FloatProcessor(ow, oh)
        val vertEdge = w - size%w
        val horzEdge = h - size%h
        for(ox in 0..(ow-1)) {
            for(oy in 0..(oh-1)) {
                out.setf(ox, oy, image.getf((ox + vertEdge)%w,(oy + horzEdge)%h))
            }
        }
        
        return out
    }
    
}
