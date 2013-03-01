package ThunderSTORM;

import Jama.Matrix;
import LM.LM;
import LM.LMfunc;

public final class Thunder_STORM {
    
    // My Symmetric Gaussian PSF
    // A/(2*pi*\sigma^2)*exp(-((((x-x_0)^2)/(2*\sigma^2))+(((y-y_0)^2)/(2*\sigma^2))))+b
    
    
    /**
     * Replicate the example in NR, fit a sum of Gaussians to data. y(x) = \sum
     * B_k exp(-((x - E_k) / G_k)^2) minimize chisq = \sum { y[j] - \sum B_k
     * exp(-((x_j - E_k) / G_k)^2) }^2
     *
     * B_k, E_k, G_k are stored in that order
     *
     * Works, results are close to those from the NR example code.
     */
    static class LMGaussTest implements LMfunc {

        static double SPREAD = 0.001; 	// noise variance

        @Override
        public double val(double[] x, double[] a) {
            assert x.length == 1;
            assert (a.length % 3) == 0;

            int K = a.length / 3;
            int i = 0;

            double y = 0.;
            for (int j = 0; j < K; j++) {
                double arg = (x[0] - a[i + 1]) / a[i + 2];
                double ex = Math.exp(-arg * arg);
                y += (a[i] * ex);
                i += 3;
            }

            return y;
        } //val

        /**
         * <pre>
         * y(x) = \sum B_k exp(-((x - E_k) / G_k)^2)
         * arg  =  (x-E_k)/G_k
         * ex   =  exp(-arg*arg)
         * fac =   B_k * ex * 2 * arg
         *
         * d/dB_k = exp(-((x - E_k) / G_k)^2)
         *
         * d/dE_k = B_k exp(-((x - E_k) / G_k)^2) . -2((x - E_k) / G_k) . -1/G_k
         *        = 2 * B_k * ex * arg / G_k
         *   d/E_k[-((x - E_k) / G_k)^2] = -2((x - E_k) / G_k) d/dE_k[(x-E_k)/G_k]
         *   d/dE_k[(x-E_k)/G_k] = -1/G_k
         *
         * d/G_k = B_k exp(-((x - E_k) / G_k)^2) . -2((x - E_k) / G_k) . -(x-E_k)/G_k^2
         *       = B_k ex -2 arg -arg / G_k
         *       = fac arg / G_k
         *   d/dx[1/x] = d/dx[x^-1] = -x[x^-2]
         */
        @Override
        public double grad(double[] x, double[] a, int a_k) {
            assert x.length == 1;

            // i - index one of the K Gaussians
            int i = 3 * (a_k / 3);

            double arg = (x[0] - a[i + 1]) / a[i + 2];
            double ex = Math.exp(-arg * arg);
            double fac = a[i] * ex * 2. * arg;

            if (a_k == i) {
                return ex;
            } else if (a_k == (i + 1)) {
                return fac / a[i + 2];
            } else if (a_k == (i + 2)) {
                return fac * arg / a[i + 2];
            } else {
                System.err.println("bad a_k");
                return 1.;
            }

        } //grad

        @Override
        public double[] initial() {
            double[] a = new double[6];
            a[0] = 4.5;
            a[1] = 2.2;
            a[2] = 2.8;

            a[3] = 2.5;
            a[4] = 4.9;
            a[5] = 2.8;
            return a;
        } //initial

        @Override
        public Object[] testdata() {
            Object[] o = new Object[4];
            int npts = 100;
            double[][] x = new double[npts][1];
            double[] y = new double[npts];
            double[] s = new double[npts];
            double[] a = new double[6];

            a[0] = 5.0;	// values returned by initial
            a[1] = 2.0;	// should be fairly close to these
            a[2] = 3.0;
            a[3] = 2.0;
            a[4] = 5.0;
            a[5] = 3.0;

            for (int i = 0; i < npts; i++) {
                x[i][0] = 0.1 * (i + 1);	// NR always counts from 1
                y[i] = val(x[i], a);
                s[i] = SPREAD * y[i];
                System.out.println(i + ": x,y= " + x[i][0] + ", " + y[i]);
            }

            o[0] = x;
            o[1] = a;
            o[2] = y;
            o[3] = s;

            return o;
        } //testdata
    } //LMGaussTest

    // test program
    public static void main(String[] cmdline) {

        LMfunc f = new LMGaussTest();

        double[] aguess = f.initial();
        Object[] test = f.testdata();
        double[][] x = (double[][]) test[0];
        double[] areal = (double[]) test[1];
        double[] y = (double[]) test[2];
        double[] s = (double[]) test[3];
        boolean[] vary = new boolean[aguess.length];
        for (int i = 0; i < aguess.length; i++) {
            vary[i] = true;
        }
        assert aguess.length == areal.length;

        try {
            LM.solve(x, aguess, y, s, vary, f, 0.001, 0.01, 100, 0);
        } catch (Exception ex) {
            System.err.println("Exception caught: " + ex.getMessage());
            System.exit(1);
        }

        System.out.print("desired solution ");
        (new Matrix(areal, areal.length)).print(10, 2);

        System.exit(0);
    } //main
    
}
