package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.FloatProcessor;

/**
 * Helper class to offer some additional functionality over the ImageProcessor from ImageJ.
 */
public class ImageProcessor {

    /**
     * Subtract two images from each other.
     * 
     * The images are required to be of the same size.
     *
     * @param fp1 an input image which the other input image ({@code fp2}) will be subtracted from
     * @param fp2 another input image which will be subtracted from {@code fp1}
     * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fp3 = fp1 - fp2}
     */
    public static FloatProcessor subtractImage(FloatProcessor fp1, FloatProcessor fp2) {
        assert (fp1.getWidth() == fp2.getWidth());
        assert (fp1.getHeight() == fp2.getHeight());

        FloatProcessor out = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
        for (int i = 0, im = fp1.getWidth(); i < im; i++) {
            for (int j = 0, jm = fp1.getHeight(); j < jm; j++) {
                out.setf(i, j, fp1.getPixelValue(i, j) - fp2.getPixelValue(i, j));
            }
        }

        return out;
    }
    
    /**
     * Crop an input image to a specified region of interest (ROI).
     * 
     * @param im an input image
     * @param left X coordinate of the left side of a ROI
     * @param top Y coordinate of the top side of a ROI
     * @param width width of a ROI
     * @param height height of a ROI
     * @return a <strong>new instance</strong> of FloatProcessor that contains the cropped image
     */
    public static FloatProcessor cropImage(FloatProcessor im, int left, int top, int width, int height) {
        assert(im != null);
        assert((width > 0) && (height > 0));
        assert((left >= 0) && (top >= 0) && ((left+width) <= im.getWidth()) && ((top+height) <= im.getHeight()));
        
        im.setRoi(left, top, width, height);
        return (FloatProcessor) im.crop();
    }

    /**
     * Apply a binary {@code threshold} to the {@code image}.
     * 
     * Instead of implicitly setting the pixels in thresholded image to 0 and 1,
     * the pixels with their values equal or greater than threshold are set
     * to {@code high_val}. The rest is res to {@code low_value}.
     * 
     * Note that this method <strong>modifies</strong> the input image.
     * 
     * @param image an input image
     * @param threshold a threshold value
     * @param low_val value that the pixels with values lesser then {@code threshold} are set to
     * @param high_val value that the pixels with values equal or greater then {@code threshold} are set to
     */
    public static void threshold(FloatProcessor image, float threshold, float low_val, float high_val) {
        for (int i = 0, im = image.getWidth(); i < im; i++) {
            for (int j = 0, jm = image.getHeight(); j < jm; j++) {
                if (image.getPixelValue(i, j) >= threshold) {
                    image.setf(i, j, high_val);
                } else {
                    image.setf(i, j, low_val);
                }
            }
        }
    }

    /**
     * Apply a {@code mask} to an input {@code image}.
     * 
     * The masking works simply by checking where is a 0 in the mask image and
     * setting the corresponding pixel in the input image to 0 as well. Hence the
     * mask does not have to be binary.
     * 
     * For example let's have the following input image:
     * <pre>
     * {@code
     * 123
     * 456
     * 789}
     * </pre>
     * and the following mask:
     * <pre>
     * {@code
     * 560
     * 804
     * 032}
     * </pre>
     * Then the result of applying the mask is:
     * <pre>
     * {@code
     * 120
     * 406
     * 089}
     * </pre>
     * 
     * @param image an input image
     * @param mask a mask image
     * @return a <strong>new instance</strong> of FloatProcessor that contains the input image after the mask was applied
     */
    public static FloatProcessor applyMask(FloatProcessor image, FloatProcessor mask) {
        assert (image.getWidth() == mask.getWidth());
        assert (image.getHeight() == mask.getHeight());

        FloatProcessor result = new FloatProcessor(image.getWidth(), image.getHeight(), (float[]) image.getPixelsCopy());
        for (int x = 0, xm = image.getWidth(); x < xm; x++) {
            for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                if (mask.getf(x, y) == 0.0f) {
                    result.setf(x, y, 0.0f);
                }
            }
        }
        
        return result;
    }
}
