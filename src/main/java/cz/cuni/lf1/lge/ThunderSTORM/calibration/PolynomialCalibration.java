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

    public static final String name = DefocusFunctionPoly.name;
    transient DefocusFunction s1Par, s2Par;

    public PolynomialCalibration() {
        super();
        s1Par = null;
        s2Par = null;
    }

    public PolynomialCalibration(double angle, double w01, double a1, double b1, double c1, double d1, double w02, double a2, double b2, double c2, double d2) {
        super(angle, w01, a1, b1, c1, d1, w02, a2, b2, c2, d2);
        s1Par = null;
        s2Par = null;
    }

    public PolynomialCalibration(double angle, DefocusFunction sigma1Params, DefocusFunction sigma2Params) {
        super(angle, sigma1Params.getW0(), sigma1Params.getA(), sigma1Params.getB(), sigma1Params.getC(), sigma1Params.getD(),
                     sigma2Params.getW0(), sigma2Params.getA(), sigma2Params.getB(), sigma2Params.getC(), sigma2Params.getD());
        s1Par = sigma1Params;
        s2Par = sigma2Params;
    }

    @Override
    public String getName() {
        return name;
    }

    // ---------------- SIGMA -------------- //

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

    // ---------------- SIGMA^2 -------------- //

    @Override
    public double evalDefocus2(double z, double w0, double a, double b, double c, double d) {
        return sqr(evalDefocus(z, w0, a, b, c, d));
    }

    private double dw2(double z, double w0, double a, double b, double c, double d) {
        double zc1 = z - c, zc2 = zc1 * zc1, zc3 = zc2 * zc1;
        return ((4*a*zc1 + 6*d*zc2) * (a*zc2 + b + d*zc3));
    }

    @Override
    public double dwx2(double z) {
        return dw2(z, w01, a1, b1, c1, d1);
    }

    @Override
    public double dwy2(double z) {
        return dw2(z, w02, a2, b2, c2, d2);
    }

    @Override
    public DaostormCalibration getDaoCalibration() {
        // TODO: re-fit the model!
        return null;
    }
}
