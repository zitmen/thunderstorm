package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.random.RandomVectorGenerator;

// TODO: add angle and higher degrees of the polynomial as in the Huang's paper (now we use Babcock's simplified model)
public class DaostormCalibration implements CylindricalLensCalibration {

    final String name = "3D DAOSTORM calibration";
    double angle;
    // sigma(z) = w_0 * sqrt(1 + ((z - c) / d)^2)
    double w0;
    double d;
    double c1;
    double c2;

    public DaostormCalibration() {
    }

    @Override
    /**
     * Z calculation as from the supplement of this paper: Three-Dimensional
     * Super-Resolution Imaging by Stochastic Optical Reconstruction Microscopy
     * by Bo Huang, Wenqin Wang, Mark Bates, Xiaowei Zhuang
     */
    public double getZ(final double sigma1, final double sigma2) {
        //multistart optimizer that does optimization with multiple starting parameters (-500,-300,-100,100,300,500)
        MultiStartMultivariateOptimizer optim = new MultiStartMultivariateOptimizer(new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE, new SimplePointChecker<PointValuePair>(1e-10, 1e-10)),
                7, new RandomVectorGenerator() {
            double value = -700;

            @Override
            public double[] nextVector() {
                value += 200;
                return new double[]{value};
            }
        });
        ObjectiveFunction distanceFunction = new ObjectiveFunction(new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double z = point[0];

                double s1calib = DaostormCalibration.evalDefocus(z, w0, d, c1);
                double s2calib = DaostormCalibration.evalDefocus(z, w0, d, c2);
                return sqrt(sqr(sqrt(sigma1) - sqrt(s1calib)) + sqr(sqrt(sigma2) - sqrt(s2calib)));
            }
        });
        ObjectiveFunctionGradient gradientFunction = new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] point) throws IllegalArgumentException {
                /* Maple derivation:
                   w0 = 2; cx = 150; cy = -150; d = 400;
                   f := z -> sqrt((sqrt(s_1)-sqrt(w_0*sqrt(1+(z-c_1)^2/d^2)))^2+(sqrt(s_2)-sqrt(w_0*sqrt(1+(z-c_2)^2/d^2)))^2);
                   dfz := diff(f(z), z);
                */
                double z = point[0];

                double sqrtS1 = sqrt(sigma1);
                double sqrtS2 = sqrt(sigma2);
                double sqrtS1calib = sqrt(DaostormCalibration.evalDefocus(z, w0, d, c1));
                double sqrtS2calib = sqrt(DaostormCalibration.evalDefocus(z, w0, d, c1));

                double upper = ((w0 * (z - c1) * (sqrtS1 - sqrtS1calib)) / (sqrtS1calib * sqrt(1 + sqr((z - c1)/d)) * sqr(d)))
                             + ((w0 * (z - c2) * (sqrtS2 - sqrtS2calib)) / (sqrtS2calib * sqrt(1 + sqr((z - c2)/d)) * sqr(d)));
                double lower = sqrt(sqr(sqrtS1 - sqrtS1calib) + sqr(sqrtS2 - sqrtS2calib));

                return new double[]{-0.5 * upper / lower};
            }
        });

        PointValuePair result = optim.optimize(gradientFunction, distanceFunction, new InitialGuess(new double[]{0}), new MaxEval(3000), GoalType.MINIMIZE);
        double distance = result.getValue();
        return result.getPointRef()[0];
    }

    public static double evalDefocus(double z, double w0, double d, double c) {
        double w = w0 * sqrt(1 + sqr((z - c)/d));
        return (w / 2.0);   // sigma = w/2
    }

    @Override
    public double getAngle() {
        return 0.0;
    }

    public double getW0() { return w0; }

    public void setW0(double w0) {
        this.w0 = w0;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    @Override
    public double getSigma1(double z) { return DaostormCalibration.evalDefocus(z, w0, d, c1); }

    @Override
    public double getSigma2(double z) { return DaostormCalibration.evalDefocus(z, w0, d, c2); }
}
