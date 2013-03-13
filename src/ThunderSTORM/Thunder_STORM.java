package ThunderSTORM;

import Jama.Matrix;
import static ThunderSTORM.utils.Math.sqr;
import static ThunderSTORM.utils.ImageProcessor.subtractImage;
import static ThunderSTORM.utils.ImageProcessor.threshold;
import static ThunderSTORM.utils.ImageProcessor.applyMask;
import ThunderSTORM.utils.Convolution;
import LMA.LMA;
import LMA.LMAMultiDimFunction;
import Watershed.WatershedAlgorithm;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import java.rmi.UnexpectedException;

public final class Thunder_STORM {

    public static class Gaussian extends LMAMultiDimFunction {

        @Override
        public double getY(double x[], double[] a) {
            // a = {x0,y0,Intensity,sigma,background}
            return a[2] / 2.0 / Math.PI / sqr(a[3]) * Math.exp(-(sqr(x[0] - a[0]) + sqr(x[1] - a[1])) / 2.0 / sqr(a[3])) + a[4];
        }

        @Override
        public double getPartialDerivate(double x[], double[] a, int parameterIndex) {
            double arg = sqr(x[0] - a[0]) + sqr(x[1] - a[1]);
            switch (parameterIndex) {
                case 0:
                    return a[2] / 2.0 / Math.PI / Math.pow(a[3], 4) * (x[0] - a[0]) * Math.exp(-arg / 2.0 / sqr(a[3])); // x0
                case 1:
                    return a[2] / 2.0 / Math.PI / Math.pow(a[3], 4) * (x[1] - a[1]) * Math.exp(-arg / 2.0 / sqr(a[3])); // y0
                case 2:
                    return Math.exp(-arg / 2.0 / sqr(a[3])) / 2.0 / Math.PI / sqr(a[3]); // Intensity
                case 3:
                    return a[2] / 2.0 / Math.PI / Math.pow(a[3], 5) * (arg - 2.0 * sqr(a[3])) * Math.exp(-arg / 2.0 / sqr(a[3])); // sigma
                case 4:
                    return 1.0; // background
            }
            throw new RuntimeException("No such parameter index: " + parameterIndex);
        }
    }

    public static FloatProcessor WaveletDetector(FloatProcessor image, boolean third_plane, boolean watershed, boolean upsample) throws UnexpectedException {
        assert (!((upsample == true) && (watershed == false))) : "Upsampling can be performed only along with watershed transform!";

        // wavelets definition
        float[] g1 = new float[]{1f / 16f, 1f / 4f, 3f / 8f, 1f / 4f, 1f / 16f};
        float[] g2 = new float[]{1f / 16f, 0f, 1f / 4f, 0f, 3f / 8f, 0f, 1f / 4f, 0f, 1f / 16f};
        float[] g3 = new float[]{1f / 16f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 3f / 8f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 1f / 16f};

        // prepare the wavelets for convolution
        FloatProcessor k1 = Convolution.getSeparableKernelFromVectors(g1, g1);
        FloatProcessor k2 = Convolution.getSeparableKernelFromVectors(g2, g2);
        FloatProcessor k3 = Convolution.getSeparableKernelFromVectors(g3, g3);

        // convolve with the wavelets
        FloatProcessor V1 = Convolution.Convolve(image, k1, Convolution.PADDING_DUPLICATE);
        FloatProcessor V2 = Convolution.Convolve(image, k2, Convolution.PADDING_DUPLICATE);
        FloatProcessor V3 = null;
        if (third_plane) {
            V3 = Convolution.Convolve(image, k3, Convolution.PADDING_DUPLICATE);
        }

        // create wavelet planes
        FloatProcessor first_plane = subtractImage(image, V1); // 1st
        FloatProcessor final_plane = subtractImage(V1, V2);    // 2nd
        if (third_plane) {
            final_plane = subtractImage(V2, V3);  // 3rd
        }
        // detection - thresholding
        threshold(final_plane, 1.25f * (float) first_plane.getStatistics().stdDev, 1.0f, 0.0f); // these are in reverse (1=low,0=high) on purpose!

        // detection - watershed transform with[out] upscaling
        if (watershed) {
            if (upsample) {
                final_plane.setInterpolationMethod(FloatProcessor.NEAREST_NEIGHBOR);
                final_plane = (FloatProcessor) final_plane.resize(final_plane.getWidth() * 2);
            }
            // run the watershed algorithm - it works only with ByteProcessor! that's all I need though
            FloatProcessor w = (FloatProcessor) WatershedAlgorithm.run((ByteProcessor) final_plane.convertToByte(false)).convertToFloat();
            final_plane = applyMask(w, final_plane);
            if (upsample) {
                final_plane = (FloatProcessor) final_plane.resize(final_plane.getWidth() / 2);
            }
        }

        // detection - finding a center of gravity (subpixel precision)
        // 1. rozdelit na komponenty grafu
        // 2. centroid
        
        // but since all other detectors return positions with pixel precision, round the positions to pixels
        // 3. zaokrouhlit na cele pixely

        return final_plane;
    }

    public static void main(String[] args) throws UnexpectedException {
        /**
         * FITTING of 2D SYMMETRIC GAUSSIAN // // Generate the data double[]
         * gen_params = new double[]{0.3, -0.8, 1.5, 1.8, 0.0}; double[][] x =
         * new double[11 * 11][2]; double[] y = new double[11 * 11]; Gaussian
         * gauss = new Gaussian(); for (int r = 0; r < 11; r++) { for (int c =
         * 0; c < 11; c++) { int idx = r * 11 + c; x[idx][0] = c - 5; // x
         * x[idx][1] = r - 5; // y y[idx] = gauss.getY(x[idx], gen_params); //
         * G(x,y) } } // // Fit the parameters to recieve `gen_params` // params
         * = {x0,y0,Intensity,sigma,background} double[] init_guess = new
         * double[]{0.0, 0.0, 1.0, 1.0, 0.0}; LMA lma = new LMA(new Gaussian(),
         * init_guess, y, x); lma.fit(); // // Print out the results
         * System.out.println("iterations: " + lma.iterationCount);
         * System.out.println( "chi2: " + lma.chi2 + ",\n" + "param0: " +
         * lma.parameters[0] + ",\n" + "param1: " + lma.parameters[1] + ",\n" +
         * "param2: " + lma.parameters[2] + ",\n" + "param3: " +
         * lma.parameters[3] + ",\n" + "param4: " + lma.parameters[4]);
         */
        /**
         * WAVELET DETECTOR
         */
        ImagePlus image = IJ.openImage("../rice.png");
        FloatProcessor fp = WaveletDetector((FloatProcessor) image.getProcessor().convertToFloat(), false, true, false);
        image.setProcessor(fp.convertToByte(false));
        IJ.save(image, "../rice_g1.png");
        /**/
    }
}
