package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DoubleDefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.log;
import static java.lang.Math.abs;

public class BiplaneEllipticGaussianPSF extends PSFModel {

    private boolean useNumericalDerivatives;
    private DoubleDefocusCalibration<DaostormCalibration> calibration;
    private double fi1, fi2, sinfi1, sinfi2, cosfi1, cosfi2;

    public BiplaneEllipticGaussianPSF(DoubleDefocusCalibration<DaostormCalibration> calibration, boolean numericalDerivatives) {
        assert(calibration != null);
        assert(calibration.homography != null);
        assert(calibration.cal1 != null);
        assert(calibration.cal2 != null);
        this.useNumericalDerivatives = numericalDerivatives;
        this.calibration = calibration;
        this.fi1 = calibration.cal1.angle;
        this.sinfi1 = Math.sin(fi1);
        this.cosfi1 = Math.cos(fi1);
        this.fi2 = calibration.cal2.angle;
        this.sinfi2 = Math.sin(fi2);
        this.cosfi2 = Math.cos(fi2);
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        throw new NotImplementedException();
    }

    @Override
    public double[] getInitialParams(SubImage subImage) {
        throw new NotImplementedException();
    }

    @Override
    public double getDoF() {
        return 6;
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.cal1.getSigma1Squared(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.cal1.getSigma2Squared(parameters[Params.Z]);
        transformed[Params.SIGMA3] = calibration.cal2.getSigma1Squared(parameters[Params.Z]);
        transformed[Params.SIGMA4] = calibration.cal2.getSigma2Squared(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqr(parameters[Params.INTENSITY]);
        transformed[Params.OFFSET1] = sqr(parameters[Params.OFFSET1]);
        transformed[Params.OFFSET2] = sqr(parameters[Params.OFFSET2]);
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.cal1.getSigma1(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.cal1.getSigma2(parameters[Params.Z]);
        transformed[Params.SIGMA3] = calibration.cal2.getSigma1(parameters[Params.Z]);
        transformed[Params.SIGMA4] = calibration.cal2.getSigma2(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.OFFSET1] = sqrt(abs(parameters[Params.OFFSET1]));
        transformed[Params.OFFSET2] = sqrt(abs(parameters[Params.OFFSET2]));
        return transformed;
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        if (afterFitting) {
            params[Params.SIGMA1] = calibration.cal1.getSigma1(params[Params.Z]);
            params[Params.SIGMA2] = calibration.cal1.getSigma2(params[Params.Z]);
            params[Params.SIGMA3] = calibration.cal2.getSigma1(params[Params.Z]);
            params[Params.SIGMA4] = calibration.cal2.getSigma2(params[Params.Z]);
        }
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.Z,
                Params.SIGMA1, Params.SIGMA2, Params.SIGMA3, Params.SIGMA4, Params.INTENSITY,
                Params.OFFSET1, Params.OFFSET2, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET1));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET2));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public double[] getInitialSimplex() {
        double[] steps = new double[Params.PARAMS_LENGTH];
        Arrays.fill(steps, 0.001);  // cannot be zero!
        steps[Params.X] = 1;
        steps[Params.Y] = 1;
        steps[Params.Z] = 100;
        steps[Params.INTENSITY] = 3000;
        steps[Params.OFFSET1] = 10;
        steps[Params.OFFSET2] = 10;
        return steps;
    }

    public double[] getInitialParams(SubImage plane1, SubImage plane2) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = (plane1.detectorX + plane2.detectorX) / 2.0;
        guess[Params.Y] = (plane1.detectorY + plane2.detectorY) / 2.0;
        guess[Params.Z] = 0;
        guess[Params.INTENSITY] = (plane1.getMax() - plane1.getMin()) + (plane2.getMax() - plane2.getMin());
        guess[Params.OFFSET1] = plane1.getMin();
        guess[Params.OFFSET2] = plane2.getMin();
        return guess;
    }

    public MultivariateVectorFunction getValueFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) throws IllegalArgumentException {
                double[] tparams = transformParameters(params);
                double[] retVal = new double[xgrid1.length + xgrid2.length];

                params[Params.SIGMA1] = calibration.cal1.getSigma1(tparams[Params.Z]);
                params[Params.SIGMA2] = calibration.cal1.getSigma2(tparams[Params.Z]);
                params[Params.SIGMA3] = calibration.cal2.getSigma1(tparams[Params.Z]);
                params[Params.SIGMA4] = calibration.cal2.getSigma2(tparams[Params.Z]);

                int index = 0;
                for(int i = 0; i < xgrid1.length; i++, index++) {
                    double dx = ((xgrid1[i] - tparams[Params.X])*cosfi1 - (ygrid1[i] - tparams[Params.Y])*sinfi1);
                    double dy = ((xgrid1[i] - tparams[Params.X])*sinfi1 + (ygrid1[i] - tparams[Params.Y])*cosfi1);
                    retVal[index] = exp(-0.5 * (sqr(dx/params[Params.SIGMA1]) + sqr(dy/params[Params.SIGMA2])))
                                  * (tparams[Params.INTENSITY] / 2.0) / (2*Math.PI*params[Params.SIGMA1]*params[Params.SIGMA2])
                                  + tparams[Params.OFFSET1];
                }
                for(int i = 0; i < xgrid2.length; i++, index++) {
                    double dx = ((xgrid2[i] - tparams[Params.X])*cosfi2 - (ygrid2[i] - tparams[Params.Y])*sinfi2);
                    double dy = ((xgrid2[i] - tparams[Params.X])*sinfi2 + (ygrid2[i] - tparams[Params.Y])*cosfi2);
                    retVal[index] = exp(-0.5 * (sqr(dx/params[Params.SIGMA3]) + sqr(dy/params[Params.SIGMA4])))
                            * (tparams[Params.INTENSITY] / 2.0) / (2*Math.PI*params[Params.SIGMA3]*params[Params.SIGMA4])
                            + tparams[Params.OFFSET2];
                }
                return retVal;
            }
        };
    }

    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return useNumericalDerivatives
                ? getNumericJacobianFunction(xgrid1, ygrid1, xgrid2, ygrid2)
                : getAnalyticJacobianFunction(xgrid1, ygrid1, xgrid2, ygrid2);
    }

    private MultivariateMatrixFunction getAnalyticJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double[][] retVal = new double[xgrid1.length + xgrid2.length][transformedPoint.length];

                int index = 0;
                for (int i = 0; i < xgrid1.length; i++, index++) {
                    double xd = xgrid1[i] - transformedPoint[Params.X];
                    double yd = ygrid1[i] - transformedPoint[Params.Y];
                    double cosfiXd = cosfi1 * xd, cosfiYd = cosfi1 * yd;
                    double sinfiYd = sinfi1 * yd, sinfiXd = sinfi1 * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first)/transformedPoint[Params.SIGMA1] + sqr(second)/transformedPoint[Params.SIGMA2]));
                    double oneDivPISS2 = 1 / (Math.PI * point[Params.SIGMA1] * point[Params.SIGMA2]);
                    // diff(psf, x0)
                    double pom1 = first*cosfi1/transformedPoint[Params.SIGMA1] + second*sinfi1/transformedPoint[Params.SIGMA2];
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    double pom2 = first*sinfi1/transformedPoint[Params.SIGMA1] + second*cosfi1/transformedPoint[Params.SIGMA2];
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, z0)
                    double pom4 = (transformedPoint[Params.Z] - calibration.cal1.c1) / sqr(calibration.cal1.d1
                                * (1 + sqr((transformedPoint[Params.Z] - calibration.cal1.c1) / calibration.cal1.d1)));
                    double pom5 = (transformedPoint[Params.Z] - calibration.cal1.c2) / sqr(calibration.cal1.d2
                                * (1 + sqr((transformedPoint[Params.Z] - calibration.cal1.c2) / calibration.cal1.d2)));
                    double pom3 = sqr(first) * pom4 + sqr(second) * pom5;
                    retVal[index][Params.Z] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom3
                                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom4
                                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom5;
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off1)
                    retVal[index][Params.OFFSET1] = 2 * point[Params.OFFSET1];
                }
                for (int i = 0; i < xgrid2.length; i++, index++) {
                    double xd = xgrid2[i] - transformedPoint[Params.X];
                    double yd = ygrid2[i] - transformedPoint[Params.Y];
                    double cosfiXd = cosfi2 * xd, cosfiYd = cosfi2 * yd;
                    double sinfiYd = sinfi2 * yd, sinfiXd = sinfi2 * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first)/transformedPoint[Params.SIGMA3] + sqr(second)/transformedPoint[Params.SIGMA4]));
                    double oneDivPISS2 = 1 / (Math.PI * point[Params.SIGMA3] * point[Params.SIGMA4]);
                    // diff(psf, x0)
                    double pom1 = first*cosfi2/transformedPoint[Params.SIGMA3] + second*sinfi2/transformedPoint[Params.SIGMA4];
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    double pom2 = first*sinfi2/transformedPoint[Params.SIGMA3] + second*cosfi2/transformedPoint[Params.SIGMA4];
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, z0)
                    double pom4 = (transformedPoint[Params.Z] - calibration.cal2.c1) / sqr(calibration.cal2.d1
                            * (1 + sqr((transformedPoint[Params.Z] - calibration.cal2.c1) / calibration.cal2.d1)));
                    double pom5 = (transformedPoint[Params.Z] - calibration.cal2.c2) / sqr(calibration.cal2.d2
                            * (1 + sqr((transformedPoint[Params.Z] - calibration.cal2.c2) / calibration.cal2.d2)));
                    double pom3 = sqr(first) * pom4 + sqr(second) * pom5;
                    retVal[index][Params.Z] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom3
                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom4
                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom5;
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off2)
                    retVal[index][Params.OFFSET2] = 2 * point[Params.OFFSET2];
                }

                return retVal;
            }
        };
    }

    private MultivariateMatrixFunction getNumericJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        final MultivariateVectorFunction valueFunction = getValueFunction(xgrid1, ygrid1, xgrid2, ygrid2);
        return new MultivariateMatrixFunction() {
            static final double step = 0.01;

            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[][] retVal = new double[xgrid1.length + xgrid2.length][point.length];

                for(int i = 0; i < point.length; i++) {
                    double[] newPoint = point.clone();
                    newPoint[i] = newPoint[i] + step;
                    double[] f1 = valueFunction.value(newPoint);
                    double[] f2 = valueFunction.value(point);
                    for(int j = 0; j < f1.length; j++) {
                        retVal[j][i] = (f1[j] - f2[j]) / step;
                    }
                }
                return retVal;
            }
        };
    }

    public MultivariateFunction getLikelihoodFunction(double[] xgrid1, double[] ygrid1, final double[] values1, double[] xgrid2, double[] ygrid2, final double[] values2) {
        final MultivariateVectorFunction valueFunction = getValueFunction(xgrid1, ygrid1, xgrid2, ygrid2);
        return new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double[] expectedValues = valueFunction.value(point);
                double logLikelihood = 0;
                int index = 0;
                for(int i = 0; i < values1.length; i++, index++) {
                    logLikelihood += values1[i] * Math.max(-1e6, log(expectedValues[index])) - expectedValues[index];
                }
                for(int i = 0; i < values2.length; i++, index++) {
                    logLikelihood += values2[i] * Math.max(-1e6, log(expectedValues[index])) - expectedValues[index];
                }
                return logLikelihood;
            }
        };
    }

    public double getChiSquared(double[] xgrid1, double[] ygrid1, double[] values1, double[] xgrid2, double[] ygrid2, double[] values2, double[] params, boolean weighted) {
        double minWeight = 1.0 / Math.max(VectorMath.max(values1), VectorMath.max(values2));
        double maxWeight = 1000 * minWeight;

        double[] expectedValues = getValueFunction(xgrid1, ygrid1, xgrid2, ygrid2).value(params);
        double chi2 = 0;
        int index = 0;
        for(int i = 0; i < values1.length; i++, index++) {
            double weight = 1;
            if(weighted) {
                weight = 1 / values1[i];
                if(Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
            }
            chi2 += sqr(values1[i] - expectedValues[index]) * weight;
        }
        for(int i = 0; i < values2.length; i++, index++) {
            double weight = 1;
            if(weighted) {
                weight = 1 / values2[i];
                if(Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
            }
            chi2 += sqr(values2[i] - expectedValues[index]) * weight;
        }
        return chi2;
    }
}
