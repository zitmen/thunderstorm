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

// sigma(z) = w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
public class DaostormCalibration extends CylindricalLensCalibration {

    final String name = "3D DAOSTORM calibration";
    DefocusFunction s1Par, s2Par;

    public DaostormCalibration() {
    }

    public DaostormCalibration(double angle, DefocusFunction sigma1Params, DefocusFunction sigma2Params) {
        super(angle, sigma1Params.getW0(), sigma1Params.getA(), sigma1Params.getB(), sigma1Params.getC(), sigma1Params.getD(),
                     sigma2Params.getW0(), sigma2Params.getA(), sigma2Params.getB(), sigma2Params.getC(), sigma2Params.getD());
        s1Par = sigma1Params;
        s2Par = sigma2Params;
    }

    @Override
    public double evalDefocus(double z, double w0, double a, double b, double c, double d) {
        double w = w0 * sqrt(1 + sqr((z - c)/d) + a*pow((z - c)/d,3) + b*pow((z - c)/d,4));
        return (w / 2.0);   // sigma = w/2
    }

    private double dw(double z, double w0, double a, double b, double c, double d) {
        double zsubz0 = z - c;
        return (w0 * (2*zsubz0/sqr(d) + 3*a*sqr(zsubz0)/pow(d,3) + 4*b*pow(zsubz0,3)/pow(d,4))) /
                (2*sqrt(1 + sqr(zsubz0)/sqr(d) + a*pow(zsubz0,3)/pow(d,3) + b*pow(zsubz0,4)/pow(d,4)));
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
