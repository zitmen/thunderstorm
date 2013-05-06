package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class ImageProcessor {

    /**
     *
     * @param template
     * @param width
     * @param height
     * @return
     */
    public static ij.process.ImageProcessor newImageProcessor(ij.process.ImageProcessor template, int width, int height) {
        if (template instanceof FloatProcessor) {
            return new FloatProcessor(width, height);
        } else if (template instanceof ShortProcessor) {
            return new ShortProcessor(width, height);
        } else if (template instanceof ByteProcessor) {
            return new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException("The only supported processors are FloatProcessor, ShortProcessor, and ByteProcessor.");
        }
    }

    // align subsequence of an array into its center
    /**
     *
     * @param line
     * @param start
     * @param end
     * @param fill_left
     * @param fill_right
     */
    public static void alignArray(int[] line, int start, int end, int fill_left, int fill_right) {
        assert line != null;
        assert start < end;
        assert start >= 0 && end < line.length;
        assert (line.length - (end - start)) % 2 == 0;

        int size = (line.length - (end - start)) / 2;
        for (int i = line.length - 1, im = line.length - size; i >= im; i--) {
            line[i] = fill_right;
        }
        for (int i = line.length - size - 1; i >= size; i--) {
            line[i] = line[i - size];
        }
        for (int i = 0; i < size; i++) {
            line[i] = fill_left;
        }
    }

    // [x,y] format
    /**
     *
     * @param row
     * @param rep
     * @return
     */
    public static int[][] replicateRow(int[] row, int rep) {
        assert rep > 0;

        int[][] mat = new int[row.length][rep];
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < rep; j++) {
                mat[i][j] = row[i];
            }
        }
        return mat;
    }

    // [x,y] format
    /**
     *
     * @param col
     * @param rep
     * @return
     */
    public static int[][] replicateColumn(int[] col, int rep) {
        assert rep > 0;

        int[][] mat = new int[rep][col.length];
        for (int i = 0; i < col.length; i++) {
            for (int j = 0; j < rep; j++) {
                mat[j][i] = col[i];
            }
        }
        return mat;
    }

    /**
     *
     * @param fp1
     * @param fp2
     * @return
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
     *
     * @param im
     * @param left
     * @param top
     * @param width
     * @param height
     * @return
     */
    public static FloatProcessor cropImage(FloatProcessor im, int left, int top, int width, int height) {
        assert(im != null);
        assert((width > 0) && (height > 0));
        assert((left >= 0) && (top >= 0) && ((left+width) <= im.getWidth()) && ((top+height) <= im.getHeight()));
        
        im.setRoi(left, top, width, height);
        return (FloatProcessor) im.crop();
    }

    /**
     *
     * @param image
     * @param threshold
     * @param low_val
     * @param high_val
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
     *
     * @param image
     * @param mask
     * @return
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
