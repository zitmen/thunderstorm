package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

abstract public class DefocusFunction extends IModuleUI<DefocusFunction> implements IModule {

    public double a;
    public double b;
    public double c;
    public double d;
    public double w0;
    public boolean scaledToNm;

    public DefocusFunction() {
        this(0, 0, 0, 0, 0, false);
    }

    public DefocusFunction(double w0, double a, double b, double c, double d, boolean scaledToNm) {
        this.w0 = w0;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.scaledToNm = scaledToNm;
    }

    public double getW0() {
        return w0;
    }

    public void setW0(double w0) {
        this.w0 = w0;
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

    public double value(double z) {
        return value(z, w0, a, b, c, d);
    }

    public void shiftInZ(double shiftAmmount) {
        c -= shiftAmmount;
    }

    public double[] toParArray() {
        return new double[]{w0, c, a, b, d};
    }

    public abstract double[] transformParams(double [] params);
    public abstract double[] transformParamsInverse(double [] params);
    public abstract double value(double z, double w0, double a, double b, double c, double d);
    public abstract ParametricUnivariateFunction getFittingFunction();
    public abstract DefocusFunction getNewInstance(double w0, double a, double b, double c, double d, boolean scaledToNm);
    public abstract DefocusFunction getNewInstance(double[] params, boolean scaledToNm);
    public abstract DefocusCalibration getCalibration();    // just to get an empty instance
    public abstract DefocusCalibration getCalibration(double angle, Homography.TransformationMatrix biplaneTransformation, DefocusFunction polynomS1Final, DefocusFunction polynomS2Final);
    public abstract double[] getInitialParams(double xmin, double ymin);
}
