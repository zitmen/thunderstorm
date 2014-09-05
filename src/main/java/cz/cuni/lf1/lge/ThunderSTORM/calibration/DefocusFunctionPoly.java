package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

/**
 * A polynomial function y = a*(z-c)^2 + b + d*(z-c)^3
 */
public class DefocusFunctionPoly extends DefocusFunction {

    public DefocusFunctionPoly() {
    }

    public DefocusFunctionPoly(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        super(w0, a, b, c, /*d*/0, scaledToNm);
    }

    public DefocusFunctionPoly(double[] params, boolean scaledToNm) {
        super(params[0], params[2], params[3], params[1], /*params[4]*/0, scaledToNm);
    }

    @Override
    public double value(double z, double w0, double a, double b, double c, double d) {
        double xsubx0 = z - c;
        return MathProxy.sqr(xsubx0)*a + /*MathProxy.pow(xsubx0,3)*d + */b;
    }

    @Override
    public ParametricUnivariateFunction getFittingFunction() {
        return new ParametricUnivariateFunction() {
            @Override
            public double value(double x, double... parameters) {
                return DefocusFunctionPoly.this.value(x, 1.0, parameters[2], parameters[3], parameters[1], /*parameters[4]*/0);
            }

            @Override
            public double[] gradient(double x, double... parameters) {
                double xsubx0 = x - parameters[1];
                double[] gradients = new double[5];
                // Partial derivatives of: a*(z - c)^2 + b + d*(z - c)^3
                gradients[0] = 0.0;
                gradients[1] = -2*parameters[2]*xsubx0/* - 3*parameters[4]*MathProxy.sqr(xsubx0)*/;
                gradients[2] = MathProxy.sqr(xsubx0);
                gradients[3] = 1;
                gradients[4] = 0/*MathProxy.pow(xsubx0, 3)*/;
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
    public CylindricalLensCalibration getCalibration(double angle, DefocusFunction polynomS1Final, DefocusFunction polynomS2Final) {
        return new PolynomialCalibration(angle, polynomS1Final, polynomS2Final);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%e*(z%+g)^2 %+e*(z%+g)^3 %+g", a, -c, d, -c, b);
    }
}
