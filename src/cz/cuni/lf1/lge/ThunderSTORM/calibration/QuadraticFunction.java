package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Locale;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

/**
 * A quadratic function y = A*(X-C)^2 + B
 */
public class QuadraticFunction implements ParametricUnivariateFunction {

    private double a;
    private double b;
    private double c;
    private boolean scaledToNm = false;

    public QuadraticFunction() {
    }

    public QuadraticFunction(double[] params) {
        assert params.length == 3;
        c = params[0];
        a = params[1];
        b = params[2];
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

    public QuadraticFunction convertToNm(double stageStep) {
        assert !scaledToNm;
        QuadraticFunction ret = new QuadraticFunction();
        ret.c = c * stageStep;
        ret.a = a / (stageStep * stageStep);
        ret.b = b;
        ret.scaledToNm = true;
        return ret;
    }

    public QuadraticFunction convertToFrames(double stageStep) {
        assert scaledToNm;
        QuadraticFunction ret = new QuadraticFunction();
        ret.c = c / stageStep;
        ret.a = a * (stageStep * stageStep);
        ret.b = b;
        ret.scaledToNm = false;
        return ret;
    }

    @Override
    public double value(double x, double... parameters) {
        return value(x, parameters[1], parameters[2], parameters[0]);
    }

    public static double value(double z, double a, double b, double c) {
        double xsubx0 = z - c;
        return xsubx0 * xsubx0 * a + b;
    }

    public double value(double z) {
        return value(z, a, b, c);
    }

    @Override
    public double[] gradient(double x, double... parameters) {
        double xsubx0 = x - parameters[0];
        double[] gradients = new double[3];
        gradients[0] = -2 * parameters[1] * xsubx0;
        gradients[1] = xsubx0 * xsubx0;
        gradients[2] = 1;
        return gradients;
    }

    public void shiftInZ(double shiftAmmount) {
        c -= shiftAmmount;
    }

    public double[] toParArray() {
        return new double[]{c, a, b};
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%f*(z%+f)^2 %+f", a, -c, b);
    }
}
