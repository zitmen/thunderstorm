package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;

// sigma(z) = w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
public class DaostormCalibration extends DefocusCalibration {

    transient DefocusFunction s1Par, s2Par;

    public DaostormCalibration() {
        super(DefocusFunctionSqrt.name);
        s1Par = null;
        s2Par = null;
    }

    public DaostormCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, double w01, double a1, double b1, double c1, double d1, double w02, double a2, double b2, double c2, double d2) {
        super(DefocusFunctionSqrt.name, angle, biplaneTransformation, w01, a1, b1, c1, d1, w02, a2, b2, c2, d2);
        s1Par = null;
        s2Par = null;
    }

    public DaostormCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, DefocusFunction sigma1Params, DefocusFunction sigma2Params) {
        super(DefocusFunctionSqrt.name, angle, biplaneTransformation, sigma1Params.getW0(), sigma1Params.getA(), sigma1Params.getB(), sigma1Params.getC(), sigma1Params.getD(),
                sigma2Params.getW0(), sigma2Params.getA(), sigma2Params.getB(), sigma2Params.getC(), sigma2Params.getD());
        s1Par = sigma1Params;
        s2Par = sigma2Params;
    }

    // ---------------- SIGMA -------------- //

    @Override
    public double evalDefocus(double z, double w0, double a, double b, double c, double d) {
        return (w0/2.0 * sqrt(1 + sqr((z - c)/d) + a*pow((z - c)/d,3) + b*pow((z - c)/d,4)));
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

    // ---------------- SIGMA^2 -------------- //

    @Override
    public double evalDefocus2(double z, double w0, double a, double b, double c, double d) {
        double zcd1 = (z - c) / d, zcd2 = zcd1 * zcd1, zcd3 = zcd2 * zcd1, zcd4 = zcd3 * zcd1;
        return (sqr(w0/2.0) * (1 + zcd2 + a*zcd3 + b*zcd4));
    }

    private double dw2(double z, double w0, double a, double b, double c, double d) {
        double zc1 = z - c, zc2 = zc1 * zc1, zc3 = zc2 * zc1;
        double d2 = d * d, d3 = d2 * d, d4 = d3 * d;
        return (sqr(w0/2.0) * (2*zc1/d2 + 3*a*zc2/d3 + 4*b*zc3/d4));
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
        return this;
    }
}
