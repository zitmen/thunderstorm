package ThunderSTORM;

import Jama.Matrix;
import LM.LM;
import LM.LMfunc;
import static ThunderSTORM.util.Math.sqr;

public final class Thunder_STORM {

    static class LMGaussTest implements LMfunc {

        // Symmetric Gaussian PSF
        @Override
        public double val(double[] x, double[] a) {
            // a = {x0,y0,Intensity,sigma,background}
            return a[2] / 2.0 / Math.PI / sqr(a[3]) * Math.exp(-(sqr(x[0] - a[0]) + sqr(x[1] - a[1])) / 2.0 / sqr(a[3])) + a[4];
        }

        // Gradient of the PSF
        @Override
        public double grad(double[] x, double[] a, int k) {
            double arg = sqr(x[0]-a[0])+sqr(x[1]-a[1]);
            switch(k)
            {
                case 0: return a[2]/2.0/Math.PI/Math.pow(a[3],4)*(x[0]-a[0])*Math.exp(-arg/2.0/sqr(a[3])); // x0
                case 1: return a[2]/2.0/Math.PI/Math.pow(a[3],4)*(x[1]-a[1])*Math.exp(-arg/2.0/sqr(a[3])); // y0
                case 2: return Math.exp(-arg/2.0/sqr(a[3]))/2.0/Math.PI/sqr(a[3]); // Intensity
                case 3: return a[2]/2.0/Math.PI/Math.pow(a[3],5)*(arg-2.0*sqr(a[3]))*Math.exp(-arg/2.0/sqr(a[3])); // sigma
                case 4: return 1.0; // background
            }
            throw new ArrayIndexOutOfBoundsException("Gradient index is out of range!");
        }

        @Override
        public double[] initial() {
            double[] a = new double[5];
            a[0] = 0.0;   // x0
            a[1] = 0.0;   // y0
            a[2] = 1.0;   // Intensity
            a[3] = 1.0;   // sigma
            a[4] = 0.0;   // background
            return a;
        }

        @Override
        public Object[] testdata() {
            Object[] o = new Object[4];
            int npts = 11 * 11;
            double[][] x = new double[npts][2];
            double[] y = new double[npts];
            double[] s = new double[npts];
            /*
            double[] a = new double[5];
            a[0] = 0.3;
            a[1] = -0.8;
            a[2] = 1.5;
            a[3] = 1.8;
            a[4] = 0.0;
           */
            double[] a = initial();
            
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    int idx = r * 11 + c;
                    x[idx][0] = c - 6;  // x
                    x[idx][1] = r - 6;  // y
                    s[idx] = val(x[idx], a);
                }
            }
            //
            String data = "0.0001,0.0003,0.0009,0.0021,0.0037,0.0048,0.0045,0.0031,0.0016,0.0006,0.0002,"
                    + "0.0002,0.0009,0.0028,0.0067,0.0117,0.0150,0.0141,0.0097,0.0049,0.0018,0.0005,"
                    + "0.0005,0.0020,0.0065,0.0154,0.0269,0.0344,0.0324,0.0224,0.0113,0.0042,0.0012,"
                    + "0.0008,0.0034,0.0110,0.0261,0.0455,0.0582,0.0547,0.0378,0.0192,0.0071,0.0020,"
                    + "0.0010,0.0042,0.0136,0.0324,0.0564,0.0722,0.0679,0.0469,0.0238,0.0089,0.0024,"
                    + "0.0009,0.0038,0.0124,0.0295,0.0514,0.0658,0.0619,0.0427,0.0217,0.0081,0.0022,"
                    + "0.0006,0.0026,0.0083,0.0198,0.0344,0.0441,0.0414,0.0286,0.0145,0.0054,0.0015,"
                    + "0.0003,0.0013,0.0041,0.0097,0.0169,0.0217,0.0204,0.0141,0.0071,0.0027,0.0007,"
                    + "0.0001,0.0005,0.0015,0.0035,0.0061,0.0078,0.0074,0.0051,0.0026,0.0010,0.0003,"
                    + "0.0000,0.0001,0.0004,0.0009,0.0016,0.0021,0.0020,0.0013,0.0007,0.0003,0.0001,"
                    + "0.0000,0.0000,0.0001,0.0002,0.0003,0.0004,0.0004,0.0003,0.0001,0.0000,0.0000";
            //
            String[] items = data.split(",");
            for (int i = 0; i < items.length; i++) {
                y[i] = ((Double) Double.parseDouble(items[i])).doubleValue();
            }
            //
            o[0] = x;
            o[1] = a;
            o[2] = y;
            o[3] = s;

            return o;
        }
    }

    public static void main(String[] cmdline) {

        LMfunc f = new LMGaussTest();

        double[] aguess = f.initial();  // initial guess
        Object[] test = f.testdata();
        double[][] x = (double[][]) test[0];    // data
        double[] areal = (double[]) test[1];    // real parameters of the real data = {x0,y0,Intensity,sigma,background}
        double[] y = (double[]) test[2];        // real data
        double[] s = (double[]) test[3];        // val(x,a)
        boolean[] vary = new boolean[aguess.length];    // flag - will the parameters change?
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
    }
}
