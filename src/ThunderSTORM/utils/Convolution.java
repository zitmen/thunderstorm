package ThunderSTORM.utils;

import Jama.Matrix;
import ij.process.ImageProcessor;
import java.rmi.UnexpectedException;
import static ThunderSTORM.utils.ImageProcessor.newImageProcessor;
import static ThunderSTORM.utils.ImageProcessor.alignArray;
import static ThunderSTORM.utils.ImageProcessor.replicateRow;
import static ThunderSTORM.utils.ImageProcessor.replicateColumn;
import ij.process.Blitter;
import ij.process.FloatProcessor;

public class Convolution {

    public static final int PADDING_ZERO = 1;
    public static final int PADDING_DUPLICATE = 2;
    public static final int PADDING_CYCLIC = 3;

    // TODO: split these padding methods into separate functions and
    //       implement them more efficiently...the algorithms are ok as it is now,
    //       but in case of DUPLICATE&CYCLIC there are some extra allocations,
    //       which make the whole thing slower!
    public static ImageProcessor addBorder(ImageProcessor image, int size, int type) throws UnexpectedException {

        assert size >= 0;
        assert type >= 1 && type <= 3;

        int w = image.getWidth();
        int h = image.getHeight();

        ImageProcessor out = newImageProcessor(image, w + 2 * size, h + 2 * size);

        switch (type) {
            case PADDING_ZERO:
                // fill the output image with zeros
                out.setValue(0);
                out.fill();
                // finally, insert the input image inside the output image
                out.copyBits(image, size, size, Blitter.COPY);
                break;

            case PADDING_DUPLICATE:
                // top side of border
                int left = image.getPixel(0, 0);
                int right = image.getPixel(w - 1, 0);
                int[] line = new int[out.getWidth()];
                image.getRow(0, 0, line, w);
                alignArray(line, size, out.getWidth() - size, left, right);
                ImageProcessor ip = newImageProcessor(image, w + 2 * size, size);
                ip.setIntArray(replicateRow(line, size));
                out.copyBits(ip, 0, 0, Blitter.COPY);

                // bottom side of border        
                left = image.getPixel(0, h - 1);
                right = image.getPixel(w - 1, h - 1);
                image.getRow(0, h - 1, line, w);
                alignArray(line, size, out.getWidth() - size, left, right);
                ip = newImageProcessor(image, w + 2 * size, size);
                ip.setIntArray(replicateRow(line, size));
                out.copyBits(ip, 0, h + size, Blitter.COPY);

                // left side of border
                line = new int[image.getWidth()];
                image.getColumn(0, 0, line, h);
                ip = newImageProcessor(image, size, h);
                ip.setIntArray(replicateColumn(line, size));
                out.copyBits(ip, 0, size, Blitter.COPY);

                // right side of border
                image.getColumn(w - 1, 0, line, h);
                ip = newImageProcessor(image, size, h);
                ip.setIntArray(replicateColumn(line, size));
                out.copyBits(ip, w + size, size, Blitter.COPY);

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
                throw new UnexpectedException("Unknown padding type!");
        }
        return out;
    }

    public static FloatProcessor Convolve(ImageProcessor image, FloatProcessor kernel, int padding_type) throws UnexpectedException {
        assert kernel.getWidth() % 2 == 1;
        assert kernel.getHeight() % 2 == 1;

        int kw = kernel.getWidth(), kh = kernel.getHeight(), padsize = java.lang.Math.max(kw, kh) / 2;
        ImageProcessor img = addBorder(image, padsize, padding_type);

        // convolution
        float[] result = new float[image.getWidth() * image.getHeight()];
        for (int ix = 0, ixm = image.getWidth(); ix < ixm; ix++) {
            for (int iy = 0, iym = image.getHeight(); iy < iym; iy++) {
                float pixel = 0;
                for (int kx = 0, kxm = kw; kx < kxm; kx++) {
                    for (int ky = 0, kym = kh; ky < kym; ky++) {
                        pixel += kernel.getPixelValue(kx, ky) * img.getPixelValue(padsize + ix - (kx - kw + 1), padsize + iy - (ky - kh + 1));
                    }
                }
                result[ix + iy * image.getHeight()] = pixel / (kw * kh);
            }
        }

        return new FloatProcessor(image.getWidth(), image.getHeight(), result);
    }

    public static class KernelMatrix extends Matrix {

        public KernelMatrix(float[] vals, int rows) {
            super(rows, vals.length / rows);
            //
            for (int i = 0, cols = vals.length / rows; i < vals.length; i++) {
                super.set(i / cols, i % cols, (double) vals[i]);
            }
        }

        private KernelMatrix(Matrix m) {
            super(m.getArray());
        }

        public float[] getData() {
            double[][] arr = super.getArray();
            if (arr == null) {
                return null;
            }
            if (arr.length == 0) {
                return null;
            }
            // serialize a matrix into a vector and perform a convesion from double to float
            float[] data = new float[arr.length * arr[0].length];
            for (int i = 0; i < arr.length; i++) {
                for (int j = 0; j < arr[i].length; j++) {
                    data[j + i * arr.length] = (float) arr[i][j];
                }
            }
            return data;
        }

        public KernelMatrix times(KernelMatrix m) {
            return new KernelMatrix(super.times((Matrix) m));
        }
    }

    public static FloatProcessor getSeparableKernelFromVectors(float[] kx, float[] ky) {
        KernelMatrix mx = new KernelMatrix(kx, kx.length);
        KernelMatrix my = new KernelMatrix(ky, 1);
        return new FloatProcessor(kx.length, ky.length, mx.times(my).getData());
    }
}
