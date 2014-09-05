package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
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

// sigma(z) = a*(z-c)^2 + b + d*(z-c)^3
public class PolynomialCalibration extends CylindricalLensCalibration {

    final String name = "Polynomial calibration";
    DefocusFunction s1Par, s2Par;

    public PolynomialCalibration() {
    }

    public PolynomialCalibration(double angle, DefocusFunction sigma1Params, DefocusFunction sigma2Params) {
        super(angle, sigma1Params.getW0(), sigma1Params.getA(), sigma1Params.getB(), sigma1Params.getC(), sigma1Params.getD(),
                     sigma2Params.getW0(), sigma2Params.getA(), sigma2Params.getB(), sigma2Params.getC(), sigma2Params.getD());
        s1Par = sigma1Params;
        s2Par = sigma2Params;
    }

    @Override
    public double evalDefocus(double z, double w0, double a, double b, double c, double d) {
        return b + a*sqr(z - c) + d*pow(z - c,3);
    }

    private double dw(double z, double w0, double a, double b, double c, double d) {
        double zsubz0 = z - c;
        return 2*a*zsubz0 + 3*d*zsubz0;
    }

    @Override
    public double dwx(double z) {
        return dw(z, w01, a1, b1, c1, d1);
    }

    @Override
    public double dwy(double z) {
        return dw(z, w02, a2, b2, c2, d2);
    }
}
