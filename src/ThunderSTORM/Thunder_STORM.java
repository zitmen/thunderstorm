package ThunderSTORM;

import Jama.Matrix;
import static ThunderSTORM.utils.Math.sqr;
import ThunderSTORM.utils.Convolution;
import LMA.LMA;
import LMA.LMAMultiDimFunction;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
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

    public static ImageProcessor WaveletDetector(ImageProcessor image) throws UnexpectedException {
        //double[] g1 = new double[]{1/16,1/4,3/8,1/4,1/16};
        //double[] g2 = new double[]{1/16,0,1/4,0,3/8,0,1/4,0,1/16};
        //double[] g3 = new double[]{1/16,0,0,0,1/4,0,0,0,3/8,0,0,0,1/4,0,0,0,1/16};

        // pad image on edges with closest point
        //int border = g3.length/2;
        //ImageProcessor imagePadded = Convolution.addBorder(image,Convolution.PADDING_DUPLICATE,border);

        //FloatProcessor g1 = new FloatProcessor(5, 1, new float[]{1f / 16f, 1f / 4f, 3f / 8f, 1f / 4f, 1f / 16f});
        //FloatProcessor g2 = new FloatProcessor(9, 1, new float[]{1f / 16f, 0f, 1f / 4f, 0f, 3f / 8f, 0f, 1f / 4f, 0f, 1f / 16f});
        //FloatProcessor g3 = new FloatProcessor(17, 1, new float[]{1f / 16f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 3f / 8f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 1f / 16f});

        float[] g1 = new float[]{1f / 16f, 1f / 4f, 3f / 8f, 1f / 4f, 1f / 16f};
        float[] g2 = new float[]{1f / 16f, 0f, 1f / 4f, 0f, 3f / 8f, 0f, 1f / 4f, 0f, 1f / 16f};
        float[] g3 = new float[]{1f / 16f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 3f / 8f, 0f, 0f, 0f, 1f / 4f, 0f, 0f, 0f, 1f / 16f};

        FloatProcessor kernel = Convolution.getSeparableKernelFromVectors(g1, g1);
        
        // TODO: vysledek konvoluce je nejakej svetlejsi nez V1!!
        // zkusit to na datech nebo jadru, kde poznam snadno rozdil!
        
        return Convolution.Convolve(image, kernel, Convolution.PADDING_DUPLICATE);
        /*
         %convolve
         V1 = conv2(g1, g1, imagePadded, 'same');
         V2 = conv2(g2, g2, V1, 'same');
         if(thirdPlane)
         V3 = conv2(g3, g3, V2, 'same');
         end

         %remove padded border
         V1 = V1(border+1:size(V1,1)-border,border+1:size(V1,2)-border);
         V2 = V2(border+1:size(V2,1)-border,border+1:size(V2,2)-border);
         if(thirdPlane)
         V3 = V3(border+1:size(V3,1)-border,border+1:size(V3,2)-border);
         end

         %create wavelet planes
         first = double(image)-V1;
         final = V1-V2; %second
         if(thirdPlane)
         final = V2-V3; %third
         end

         final = final > params.threshold*std(first(:));

         if(doWatershed)
         if(doUpsample)
         final = final(ceil((1:2*size(final,1))/2), ceil((1:2*size(final,2))/2));
         end
         d = bwdist(~final);
         d = -d;
         W = watershed(d);
         W(~final) = 0;
         final = W;
         if(doUpsample)
         final = final(1:2:size(final,1),1:2:size(final,2));
         end
         end

         locstruct = regionprops(final, 'Centroid');
         loc = [locstruct.Centroid];
         loc = round(loc);
         loc = reshape(loc,2,length(loc)/2)';

         end
         */
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
        ImageProcessor ip = WaveletDetector(image.getProcessor());
        image.setProcessor(ip);
        IJ.save(image, "../rice_g1.png");
        /**/
    }
}
