package ThunderSTORM.utils;

import static ThunderSTORM.utils.ImageProcessor.alignArray;
import static ThunderSTORM.utils.ImageProcessor.replicateColumn;
import static ThunderSTORM.utils.ImageProcessor.replicateRow;
import ij.process.Blitter;
import ij.process.FloatProcessor;

public class Padding {
    
    //public static final int PADDING_NONE = 0;
    public static final int PADDING_ZERO = 1;
    public static final int PADDING_DUPLICATE = 2;
    public static final int PADDING_CYCLIC = 3;

    // TODO: split these padding methods into separate functions and
    //       implement them more efficiently...the algorithms are ok as it is now,
    //       but in case of DUPLICATE&CYCLIC there are some extra allocations,
    //       which make the whole thing slower!
    public static FloatProcessor addBorder(FloatProcessor image, int size, int type) {

        assert size >= 0;
        assert type >= 1 && type <= 3;

        int w = image.getWidth();
        int h = image.getHeight();

        FloatProcessor out = null;

        switch (type) {
            /*
            case PADDING_NONE:
                out = new FloatProcessor(w, h);
                out.copyBits(image, 0, 0, Blitter.COPY);
                break;
            */
            case PADDING_ZERO:
                out = new FloatProcessor(w + 2 * size, h + 2 * size);
                // fill the output image with zeros
                out.setValue(0);
                out.fill();
                // finally, insert the input image inside the output image
                out.copyBits(image, size, size, Blitter.COPY);
                break;

            case PADDING_DUPLICATE:
                out = new FloatProcessor(w + 2 * size, h + 2 * size);
                // top side of border
                int left = image.getPixel(0, 0);
                int right = image.getPixel(w - 1, 0);
                int[] line = new int[out.getWidth()];
                image.getRow(0, 0, line, w);
                alignArray(line, size, out.getWidth() - size, left, right);
                FloatProcessor fp = new FloatProcessor(w + 2 * size, size);
                fp.setIntArray(replicateRow(line, size));
                out.copyBits(fp, 0, 0, Blitter.COPY);

                // bottom side of border        
                left = image.getPixel(0, h - 1);
                right = image.getPixel(w - 1, h - 1);
                image.getRow(0, h - 1, line, w);
                alignArray(line, size, out.getWidth() - size, left, right);
                fp = new FloatProcessor(w + 2 * size, size);
                fp.setIntArray(replicateRow(line, size));
                out.copyBits(fp, 0, h + size, Blitter.COPY);

                // left side of border
                line = new int[image.getHeight()];
                image.getColumn(0, 0, line, h);
                fp = new FloatProcessor(size, h);
                fp.setIntArray(replicateColumn(line, size));
                out.copyBits(fp, 0, size, Blitter.COPY);

                // right side of border
                image.getColumn(w - 1, 0, line, h);
                fp = new FloatProcessor(size, h);
                fp.setIntArray(replicateColumn(line, size));
                out.copyBits(fp, w + size, size, Blitter.COPY);

                // finally, insert the input image inside the output image
                out.copyBits(image, size, size, Blitter.COPY);
                break;

            case PADDING_CYCLIC:
                // TODO
                throw new UnsupportedOperationException("Cyclic padding is not supported in this version!");
            // finally, insert the input image inside the output image
            //out.copyBits(image, size, size, Blitter.COPY);
            //break;

            default:
                throw new UnsupportedOperationException("Unsupported padding method!");
        }
        return out;
    }
    
}
