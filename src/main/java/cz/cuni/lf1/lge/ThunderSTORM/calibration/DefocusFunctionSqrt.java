package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.abs;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.pow;

import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

import javax.swing.*;

/**
 * A squared polynomial function y = w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
 *
 * Note: to avoid problems with over-fitting, the third degree of the polynomial is set to zero and the fourth degree is forced non-negative
 *
 * TODO: this can be solved using regularization put on parameters `a` and `b`, but at the moment
 *       I am not sure how to do it in a simple way using the Apache Commons Math library
 */
public class DefocusFunctionSqrt extends DefocusFunction {

    public static final String name = "Huang '08";

    public DefocusFunctionSqrt() {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public DefocusFunction getImplementation() {
        return this;
    }

    public DefocusFunctionSqrt(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        super(w0, a, b, c, d, scaledToNm);
    }

    @Override
    public double[] transformParams(double[] params) {
        double [] trans = Arrays.copyOf(params, params.length);
        trans[2] = 0;
        return trans;
    }

    @Override
    public double[] transformParamsInverse(double[] params) {
        double [] trans = Arrays.copyOf(params, params.length);
        trans[2] = 0;
        trans[3] = sqrt(abs(params[3]));
        return trans;
    }

    public DefocusFunctionSqrt(double[] params, boolean scaledToNm) {
        super(params[0], params[2], params[3], params[1], params[4], scaledToNm);
    }

    @Override
    public double value(double z, double w0, double a, double b, double c, double d) {
        double xsubx0 = z - c;
        return 0.5*w0*sqrt(1 + sqr(xsubx0/d) + a*pow(xsubx0/d,3) + b*pow(xsubx0/d,4));
    }

    @Override
    public ParametricUnivariateFunction getFittingFunction() {
        return new ParametricUnivariateFunction() {
            @Override
            public double value(double x, double... params) {
                double[] trans = transformParams(params);
                return DefocusFunctionSqrt.this.value(x, trans[0], trans[2], trans[3], trans[1], trans[4]);
            }

            @Override
            public double[] gradient(double x, double... params) {
                double[] trans = transformParams(params);
                double xsubx0 = x - trans[1];
                double[] gradients = new double[5];
                // Partial derivatives of: w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
                double dd = sqrt(1 + sqr(xsubx0/trans[4]) + trans[2]*pow(xsubx0/trans[4],3) + trans[3]*pow(xsubx0/trans[4],4));
                gradients[0] = 0.50*dd;
                gradients[1] = 0.25*trans[0]*(-2*xsubx0/sqr(trans[4]) - 3*trans[2]*sqr(xsubx0)/pow(trans[4],3) - 4*trans[3]*pow(xsubx0,3)/pow(trans[4],4))/dd;
                gradients[2] = 0.25*trans[0]*pow(xsubx0,3)/pow(trans[4],3)/dd;
                gradients[3] = 0.25*trans[0]*pow(xsubx0,4)/pow(trans[4],4)/dd;
                gradients[4] = 0.25*trans[0]*(-2*sqr(xsubx0)/pow(trans[4],3) - 3*trans[2]*pow(xsubx0,3)/pow(trans[4],4) - 4*trans[3]*pow(xsubx0,4)/pow(trans[4],5))/dd;
                return gradients;
            }
        };
    }

    @Override
    public double[] getInitialParams(double xmin, double ymin) {
        return new double[]{1, xmin, 1e-2, ymin, 400};
    }

    @Override
    public DefocusFunction getNewInstance(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        return new DefocusFunctionSqrt(w0, a, b, c, d, scaledToNm);
    }

    @Override
    public DefocusFunction getNewInstance(double[] params, boolean scaledToNm) {
        return new DefocusFunctionSqrt(params, scaledToNm);
    }

    @Override
    public DefocusCalibration getCalibration() {
        return new DaostormCalibration();
    }

    @Override
    public DefocusCalibration getCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, DefocusFunction polynomS1Final, DefocusFunction polynomS2Final) {
        return new DaostormCalibration(angle, biplaneTransformation, polynomS1Final, polynomS2Final);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%e*sqrt(1 + ((z%+g)/%e)^2 %+e*((z%+g)/%e)^3 %+e*((z%+g)/%e)^4)", w0, -c, d, a, -c, d, b, -c, d);
    }
}
