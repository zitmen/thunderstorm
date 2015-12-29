package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

// sigma(z) = a*(z-c)^2 + b + d*(z-c)^3
public class PolynomialCalibration extends DefocusCalibration {

    transient DefocusFunction s1Par, s2Par;

    public PolynomialCalibration() {
        super(DefocusFunctionPoly.name);
        s1Par = null;
        s2Par = null;
    }

    public PolynomialCalibration(double angle, Homography.TransformationMatrix biplanetransform, double w01, double a1, double b1, double c1, double d1, double w02, double a2, double b2, double c2, double d2) {
        super(DefocusFunctionPoly.name, angle, biplanetransform, w01, a1, b1, c1, d1, w02, a2, b2, c2, d2);
        s1Par = null;
        s2Par = null;
    }

    public PolynomialCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, DefocusFunction sigma1Params, DefocusFunction sigma2Params) {
        super(DefocusFunctionPoly.name, angle, biplaneTransformation, sigma1Params.getW0(), sigma1Params.getA(), sigma1Params.getB(), sigma1Params.getC(), sigma1Params.getD(),
                sigma2Params.getW0(), sigma2Params.getA(), sigma2Params.getB(), sigma2Params.getC(), sigma2Params.getD());
        s1Par = sigma1Params;
        s2Par = sigma2Params;
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
        ParametricUnivariateFunction sqrtFn = new DefocusFunctionSqrt().getFittingFunction();
        ParametricUnivariateFunction polyFn = new DefocusFunctionPoly().getFittingFunction();

        CurveFitter<ParametricUnivariateFunction> fitter1 = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-10, 10e-10)));
        CurveFitter<ParametricUnivariateFunction> fitter2 = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-10, 10e-10)));

        double [] polyParams1 = new double[] {w01, c1, a1, b1, d1};
        double [] polyParams2 = new double[] {w02, c2, a2, b2, d2};

        double zRange = ceil(2*(abs(c1)+abs(c2)));    // -zRange:+zRange
        for(double z = -zRange; z <= zRange; z += 5.0) {
            fitter1.addObservedPoint(z, polyFn.value(z, polyParams1));
            fitter2.addObservedPoint(z, polyFn.value(z, polyParams2));
        }

        double [] parSigma1 = fitter1.fit(1000, sqrtFn, new double[] {2.0, c1, 0.0, 0.0, zRange/2.0});
        double [] parSigma2 = fitter2.fit(1000, sqrtFn, new double[] {2.0, c2, 0.0, 0.0, zRange/2.0});

        return new DaostormCalibration(angle, homography, parSigma1[0], parSigma1[2], parSigma1[3], parSigma1[1], parSigma1[4],
                                                    parSigma2[0], parSigma2[2], parSigma2[3], parSigma2[1], parSigma2[4]);
    }
}
