package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

/**
 * A polynomial function y = A*(X-C)^2 + B + D*(X-C)^3
 */
public class DefocusFunction {

    private double a;
    private double b;
    private double c;
    private double d;
    private boolean scaledToNm = false;

    public DefocusFunction() {
    }

    public DefocusFunction(double[] params) {
        assert params.length == 4;
        c = params[0];
        a = MathProxy.sqr(params[1]);
        b = MathProxy.sqr(params[2]);
        d = params[3];
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }
    
    public DefocusFunction convertToNm(double stageStep) {
        assert !scaledToNm;
        DefocusFunction ret = new DefocusFunction();
        ret.c = c * stageStep;
        ret.a = a / (stageStep * stageStep);
        ret.b = b;
        ret.d = d / (stageStep * stageStep * stageStep);
        ret.scaledToNm = true;
        return ret;
    }

    public DefocusFunction convertToFrames(double stageStep) {
        assert scaledToNm;
        DefocusFunction ret = new DefocusFunction();
        ret.c = c / stageStep;
        ret.a = a * (stageStep * stageStep);
        ret.b = b;
        ret.d = d * (stageStep * stageStep * stageStep);
        ret.scaledToNm = false;
        return ret;
    }

    public static double value(double z, double a, double b, double c, double d) {
        double xsubx0 = z - c;
        return MathProxy.sqr(xsubx0) * a + MathProxy.sqr(xsubx0) * xsubx0 * d + b;
    }

    public double value(double z) {
        return value(z, a, b, c, d);
    }

    public void shiftInZ(double shiftAmmount) {
        c -= shiftAmmount;
    }

    public double[] toParArray() {
        return new double[]{c, a, b, d};
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%e*(z%+g)^2 %+e*(z%+g)^3 %+g", a, -c, d, -c, b);
    }

    public static class FittingFunction implements ParametricUnivariateFunction {

        @Override
        public double value(double x, double... parameters) {
            return DefocusFunction.value(x, MathProxy.sqr(parameters[1]), MathProxy.sqr(parameters[2]), parameters[0], parameters[3]);
        }

        @Override
        public double[] gradient(double x, double... parameters) {
            double xsubx0 = x - parameters[0];
            double[] gradients = new double[4];
            gradients[0] = (-3 * parameters[3] * xsubx0 - 2 * MathProxy.sqr(parameters[1])) * xsubx0;
            gradients[1] = MathProxy.sqr(xsubx0) * 2 * parameters[1];
            gradients[2] = 2 * parameters[2];
            gradients[3] = xsubx0 * MathProxy.sqr(xsubx0);
            return gradients;
        }
    }
}
