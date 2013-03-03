package ThunderSTORM;

import static ThunderSTORM.util.Math.sqr;
import LMA.LMA;
import LMA.LMAMultiDimFunction;

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

    public static void main(String[] args) {
        //
        // Generate the data
        double[] gen_params = new double[]{0.3, -0.8, 1.5, 1.8, 0.0};
        double[][] x = new double[11 * 11][2];
        double[] y = new double[11 * 11];
        Gaussian gauss = new Gaussian();
        for (int r = 0; r < 11; r++) {
            for (int c = 0; c < 11; c++) {
                int idx = r * 11 + c;
                x[idx][0] = c - 5;  // x
                x[idx][1] = r - 5;  // y
                y[idx] = gauss.getY(x[idx], gen_params);    // G(x,y)
            }
        }
        //
        // Fit the parameters to recieve `gen_params`
        // params = {x0,y0,Intensity,sigma,background}
        double[] init_guess = new double[]{0.0, 0.0, 1.0, 1.0, 0.0};
        LMA lma = new LMA(new Gaussian(), init_guess, y, x);
        lma.fit();
        //
        // Print out the results
        System.out.println("iterations: " + lma.iterationCount);
        System.out.println(
                "chi2: " + lma.chi2 + ",\n"
                + "param0: " + lma.parameters[0] + ",\n"
                + "param1: " + lma.parameters[1] + ",\n"
                + "param2: " + lma.parameters[2] + ",\n"
                + "param3: " + lma.parameters[3] + ",\n"
                + "param4: " + lma.parameters[4]);
    }
}
