package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Arrays;
import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

import javax.swing.*;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.pow;

/**
 * A polynomial function y = a*(z-c)^2 + b + d*(z-c)^3
 *
 * Note: to avoid problems with over-fitting, the third degree of the polynomial is set to zero
 *
 * TODO: this can be solved using regularization put on parameter `d`, but at the moment
 *       I am not sure how to do it in a simple way using the Apache Commons Math library
 */
public class DefocusFunctionPoly extends DefocusFunction {

    public static final String name = "ThunderSTORM";

    public DefocusFunctionPoly() {
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

    public DefocusFunctionPoly(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        super(w0, a, b, c, d, scaledToNm);
    }

    @Override
    public double[] transformParams(double[] params) {
        double [] trans = Arrays.copyOf(params, params.length);
        trans[4] = 0;
        return trans;
    }

    @Override
    public double[] transformParamsInverse(double[] params) {
        double [] trans = Arrays.copyOf(params, params.length);
        trans[4] = 0;
        return trans;
    }

    public DefocusFunctionPoly(double[] params, boolean scaledToNm) {
        super(params[0], params[2], params[3], params[1], params[4], scaledToNm);
    }

    @Override
    public double value(double z, double w0, double a, double b, double c, double d) {
        double xsubx0 = z - c;
        return sqr(xsubx0)*a + pow(xsubx0, 3)*d + b;
    }

    @Override
    public ParametricUnivariateFunction getFittingFunction() {
        return new ParametricUnivariateFunction() {
            @Override
            public double value(double x, double... params) {
                double[] trans = transformParams(params);
                return DefocusFunctionPoly.this.value(x, 1.0, trans[2], trans[3], trans[1], trans[4]);
            }

            @Override
            public double[] gradient(double x, double... params) {
                double[] trans = transformParams(params);
                double xsubx0 = x - trans[1];
                double[] gradients = new double[5];
                // Partial derivatives of: a*(z - c)^2 + b + d*(z - c)^3
                gradients[0] = 0.0;
                gradients[1] = -2*trans[2]*xsubx0 - 3*trans[4]*sqr(xsubx0);
                gradients[2] = sqr(xsubx0);
                gradients[3] = 1;
                gradients[4] = pow(xsubx0, 3);
                return gradients;
            }
        };
    }

    @Override
    public DefocusFunction getNewInstance(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        return new DefocusFunctionPoly(w0, a, b, c, d, scaledToNm);
    }

    @Override
    public DefocusFunction getNewInstance(double[] params, boolean scaledToNm) {
        return new DefocusFunctionPoly(params, scaledToNm);
    }

    @Override
    public DefocusCalibration getCalibration() {
        return new PolynomialCalibration();
    }

    @Override
    public DefocusCalibration getCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, DefocusFunction polynomS1Final, DefocusFunction polynomS2Final) {
        return new PolynomialCalibration(angle, biplaneTransformation, polynomS1Final, polynomS2Final);
    }

    @Override
    public double[] getInitialParams(double xmin, double ymin) {
        return new double[]{1, xmin, 1e-2, ymin, 0};
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%e*(z%+g)^2 %+e*(z%+g)^3 %+g", a, -c, d, -c, b);
    }
}
