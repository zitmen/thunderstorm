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

public class PolynomialCalibration implements CylindricalLensCalibration {

    final String name = "Polynomial calibration";
    double angle;
    // polynom = b + a*(z-c)^2
    double a1;
    double b1;
    double c1;
    double a2;
    double b2;
    double c2;

    public PolynomialCalibration() {
    }

    public PolynomialCalibration(double angle, QuadraticFunction sigma1Params, QuadraticFunction sigma2Params) {
        this.angle = angle;
        c1 = sigma1Params.getC();
        a1 = sigma1Params.getA();
        b1 = sigma1Params.getB();
        c2 = sigma2Params.getC();
        a2 = sigma2Params.getA();
        b2 = sigma2Params.getB();
    }

    @Override
    public double getZ(double sigma1, double sigma2) {
        return getZZhuang(sigma1, sigma2);
    }

    public double getZSeparate(double sigma1, double sigma2) {

        double sqrt = Math.sqrt((sigma1 - b1) / a1);
        if(Double.isNaN(sqrt)) {
            sqrt = 0;
        }
        double sqrt2 = Math.sqrt((sigma2 - b2) / a2);
        if(Double.isNaN(sqrt2)) {
            sqrt2 = 0;
        }
        //two solutions for each sigma
        double z1s1 = -sqrt + c1;
        double z2s1 = sqrt + c1;
        double z1s2 = -sqrt2 + c2;
        double z2s2 = sqrt2 + c2;


        double d11 = Math.abs(z1s1 - z1s2);
        double d12 = Math.abs(z1s1 - z2s2);
        double d22 = Math.abs(z2s1 - z2s2);

        double z;
        if(d11 < d12) {
            if(d11 < d22) {
                z = (z1s1 + z1s2) / 2;
            } else {
                z = (z2s1 + z2s2) / 2;
            }
        } else {
            if(d12 < d22) {
                z = (z1s1 + z2s2) / 2;
            } else {
                z = (z2s1 + z2s2) / 2;
            }
        }

        return z;
    }

    public double getZDiff(double sigma1, double sigma2) {
        double d = sigma1 - sigma2;
        double x;
        if(Math.abs(a1 - a2) < 1e-6) {
            x = (-a1 * sqr(c1) + a1 * sqr(c2) - b1 + b2 + d) / (2 * a1 * c2 - 2 * a1 * c1);
        } else {
            double sqrt = Math.sqrt(sqr(2 * a2 * c2 - 2 * a1 * c1) - 4 * (a1 - a2) * (a1 * sqr(c1) - a2 * sqr(c2) + b1 - b2 - d));
            double x1 = (-sqrt + 2 * a1 * c1 - 2 * a2 * c2) / (2 * (a1 - a2));
            double x2 = (sqrt + 2 * a1 * c1 - 2 * a2 * c2) / (2 * (a1 - a2));
            x = (Math.abs(x1) < Math.abs(x2)) ? x1 : x2;
        }

        return x;
    }

    /**
     * Z calculation as from the supplement of this paper: Three-Dimensional
     * Super-Resolution Imaging by Stochastic Optical Reconstruction Microscopy
     * by Bo Huang, Wenqin Wang, Mark Bates, Xiaowei Zhuang
     */
    public double getZZhuang(final double sigma1, final double sigma2) {
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
                double s1calib = b1 + a1 * sqr(z - c1);
                double s2calib = b2 + a2 * sqr(z - c2);
                return sqrt(sqr(sqrt(sigma1) - sqrt(s1calib)) + sqr(sqrt(sigma2) - sqrt(s2calib)));
            }
        });
        ObjectiveFunctionGradient gradientFunction = new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] point) throws IllegalArgumentException {
                // wolfram alpha derivation:
                // d(sqrt((sqrt(s_1) - sqrt(b_1 - a_1 *(z - c_1)^2))^2+(sqrt(s_2) - sqrt(b_2 - a_2 * (z - c_2)^2))^2))/dz
                double z = point[0];
                double sqrtS1calib = sqrt(b1 + a1 * sqr(z - c1));
                double sqrtS2calib = sqrt(b2 + a2 * sqr(z - c2));
                double upper = (2 * a1 * (z - c1) * (sqrt(sigma1) - sqrtS1calib)) / sqrtS1calib + (2 * a2 * (z - c2) * (sqrt(sigma2) - sqrtS2calib)) / sqrtS2calib;
                double lower = 2 * sqrt(sqr(sqrt(sigma1) - sqrtS1calib) + sqr(sqrt(sigma2) - sqrtS2calib));

                return new double[]{-upper / lower};
            }
        });

        PointValuePair result = optim.optimize(gradientFunction, distanceFunction, new InitialGuess(new double[]{0}), new MaxEval(3000), GoalType.MINIMIZE);
        double distance = result.getValue();
        return result.getPointRef()[0];
    }

    @Override
    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getA1() {
        return a1;
    }

    public void setA1(double a1) {
        this.a1 = a1;
    }

    public double getB1() {
        return b1;
    }

    public void setB1(double b1) {
        this.b1 = b1;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getA2() {
        return a2;
    }

    public void setA2(double a2) {
        this.a2 = a2;
    }

    public double getB2() {
        return b2;
    }

    public void setB2(double b2) {
        this.b2 = b2;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    @Override
    public double getSigma1(double z) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSigma2(double z) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
