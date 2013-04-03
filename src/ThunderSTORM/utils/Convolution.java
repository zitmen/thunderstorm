package ThunderSTORM.utils;

import Jama.Matrix;
import ij.process.FloatProcessor;

public class Convolution {

    public static FloatProcessor Convolve(FloatProcessor image, FloatProcessor kernel, int padding_type, boolean return_same_size) {
        assert kernel.getWidth() % 2 == 1;
        assert kernel.getHeight() % 2 == 1;

        // TODO: padding_NONE!! same_size!!! asi nejdriv testy!!
        
        int kw = kernel.getWidth(), kh = kernel.getHeight(), padsize = java.lang.Math.max(kw, kh) / 2;
        int iw = image.getWidth(), ih = image.getHeight(), idx;
        FloatProcessor img = (FloatProcessor) Padding.addBorder(image, padsize, padding_type);

        // convolution
        float[] result = new float[iw * ih];
        for (int ix = 0; ix < iw; ix++) {
            for (int iy = 0; iy < ih; iy++) {
                idx = iy * iw + ix;
                for (int kx = 0; kx < kw; kx++) {
                    for (int ky = 0; ky < kh; ky++) {
                        result[idx] += kernel.getPixelValue(kx, ky) * img.getPixelValue(padsize + ix + (kx - kw / 2), padsize + iy + (ky - kh / 2));
                    }
                }
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
