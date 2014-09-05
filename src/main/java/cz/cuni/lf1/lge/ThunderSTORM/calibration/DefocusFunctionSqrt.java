package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

/**
 * A squared polynomial function y = w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
 */
public class DefocusFunctionSqrt extends DefocusFunction {

    public DefocusFunctionSqrt() {
    }

    public DefocusFunctionSqrt(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        super(w0, a, b, c, d, scaledToNm);
    }

    public DefocusFunctionSqrt(double[] params, boolean scaledToNm) {
        super(params[0], params[2], params[3], params[1], params[4], scaledToNm);
    }

    @Override
    public double value(double z, double w0, double a, double b, double c, double d) {
        double xsubx0 = z - c;
        return w0*MathProxy.sqrt(1 + MathProxy.sqr(xsubx0/d) + a*MathProxy.pow(xsubx0/d,3) + b*MathProxy.pow(xsubx0/d,4));
    }

    @Override
    public ParametricUnivariateFunction getFittingFunction() {
        return new ParametricUnivariateFunction() {
            @Override
            public double value(double x, double... parameters) {
                return DefocusFunctionSqrt.this.value(x, parameters[0], parameters[2], parameters[3], parameters[1], parameters[4]);
            }

            @Override
            public double[] gradient(double x, double... parameters) {
                double xsubx0 = x - parameters[1];
                double[] gradients = new double[5];
                // Partial derivatives of: w0*sqrt(1 + ((z-c)/d)^2 + a*((z-c)/d)^3 + b*((z-c)/d)^4)
                double dd = MathProxy.sqrt(1 + MathProxy.sqr(xsubx0/d) + a*MathProxy.pow(xsubx0/d,3) + b*MathProxy.pow(xsubx0/d,4));
                gradients[0] = dd;
                gradients[1] = 0.5*w0*(-2*xsubx0/MathProxy.sqr(d) - 3*parameters[2]*MathProxy.sqr(xsubx0)/MathProxy.pow(d,3) - 4*parameters[3]*MathProxy.pow(xsubx0,3)/MathProxy.pow(d,4))/dd;
                gradients[2] = 0.5*w0*MathProxy.pow(xsubx0,3)/MathProxy.pow(d,3)/dd;
                gradients[3] = 0.5*w0*MathProxy.pow(xsubx0,4)/MathProxy.pow(d,4)/dd;
                gradients[4] = 0.5*w0*(-2*MathProxy.sqr(xsubx0)/MathProxy.pow(d,3) - 3*parameters[2]*MathProxy.pow(xsubx0, 3)/MathProxy.pow(d,4) - 4*parameters[3]*MathProxy.pow(xsubx0,4)/MathProxy.pow(d,5))/dd;
                return gradients;
            }
        };
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
    public CylindricalLensCalibration getCalibration(double angle, DefocusFunction polynomS1Final, DefocusFunction polynomS2Final) {
        return new DaostormCalibration(angle, polynomS1Final, polynomS2Final);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%e*sqrt(1 + ((z%+g)/%e)^2 %+e*((z%+g)/%e)^3 %+e*((z%+g)/%e)^4)", w0, -c, d, a, -c, d, b, -c, d);
    }
}
