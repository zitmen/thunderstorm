package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import org.apache.commons.math3.special.Erf;
import org.junit.Test;

public class IntegratedSymmetricGaussianPSFTest {

    @Test
    public void showImage() throws InterruptedException {
        IntegratedSymmetricGaussianPSF psf = new IntegratedSymmetricGaussianPSF(1.6);
        double[] params = new double[Params.PARAMS_LENGTH];
        params[Params.INTENSITY] = 1;
        params[Params.X] = 1;
        params[Params.Y] = 0;
        params[Params.SIGMA] = 1.5;
        params[Params.OFFSET] = 0;

        int bigSubImageSize = 11;
        int subimageSize = bigSubImageSize / 2;
        int[] xgrid = new int[bigSubImageSize * bigSubImageSize];
        int[] ygrid = new int[bigSubImageSize * bigSubImageSize];
        int idx = 0;
        for(int i = -subimageSize; i <= subimageSize; i++) {
            for(int j = -subimageSize; j <= subimageSize; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }

        double[] values = new double[bigSubImageSize * bigSubImageSize];
        for(int i = 0; i < values.length; i++) {
            values[i] = psf.getValue(params, xgrid[i], ygrid[i]);
        }

        //new ImagePlus("vals", new FloatProcessor(bigSubImageSize, bigSubImageSize, values)).show();
        //Thread.sleep(20000);
    }

    @Test
    public void testPerformanceErf() {
        int n = 10000000;
        long start = System.nanoTime();
        double d = 0;
        for(int i = 0; i < n; i++) {
            d += Erf.erf(i/n);
        }
        long end = System.nanoTime();
        System.out.println(d);
        System.out.println("erf: " +(end-start)/1e6/n);
    }
    
    @Test
    public void testPerformanceExp() {
        int n = 10000000;
        long start = System.nanoTime();
        double d = 0;
        for(int i = 0; i < n; i++) {
            d += Math.exp((double)i/n);
        }
        long end = System.nanoTime();
        System.out.println(d);
        System.out.println("exp: " +(end-start)/1e6/n);
    }
    @Test
    public void testPerformanceErf2() {
        int n = 10000000;
        long start = System.nanoTime();
        double d = 0;
        for(int i = 0; i < n; i++) {
            d += Erf.erf(i/n/10,i/2/n/10);
        }
        long end = System.nanoTime();
        System.out.println(d);
        System.out.println("erf2: " +(end-start)/1e6/n);
    }
    
    
}