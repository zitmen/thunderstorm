package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import static java.lang.Math.abs;

public class BiplaneSymmetricGaussianPSF extends PSFModel implements IBiplanePSFModel {

    private DaostormCalibration calibration;
    protected boolean useNumericalDerivatives;

    protected BiplaneSymmetricGaussianPSF(boolean useNumericalDerivatives) {
        calibration = null;
        this.useNumericalDerivatives = useNumericalDerivatives;
    }

    public BiplaneSymmetricGaussianPSF(DaostormCalibration calibration, boolean useNumericalDerivatives) {
        assert(calibration != null);
        assert(calibration.homography != null);
        this.calibration = calibration;
        this.useNumericalDerivatives = useNumericalDerivatives;
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        throw new NotImplementedException();
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.getSigma1Squared(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.getSigma2Squared(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqr(parameters[Params.INTENSITY]);
        transformed[Params.OFFSET1] = sqr(parameters[Params.OFFSET1]);
        transformed[Params.OFFSET2] = sqr(parameters[Params.OFFSET2]);
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.getSigma1(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.getSigma2(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.OFFSET1] = sqrt(abs(parameters[Params.OFFSET1]));
        transformed[Params.OFFSET2] = sqrt(abs(parameters[Params.OFFSET2]));
        return transformed;
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

    @Override
    public double[] getInitialParams(SubImage subImage) {
        throw new NotImplementedException();
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        if (afterFitting) {
            params[Params.SIGMA1] = calibration.getSigma1(params[Params.Z]);
            params[Params.SIGMA2] = calibration.getSigma2(params[Params.Z]);
        }
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.Z, Params.SIGMA1,
                Params.SIGMA2, Params.INTENSITY, Params.OFFSET1, Params.OFFSET2, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET1));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET2));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public double getDoF() {
        return 6;
    }

    @Override
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

    @Override
    public MultivariateVectorFunction getValueFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) throws IllegalArgumentException {
                double[] tparams = transformParameters(params);
                double[] retVal = new double[xgrid1.length + xgrid2.length];

                int index = 0;
                for(int i = 0; i < xgrid1.length; i++, index++) {
                    retVal[index] = exp(-0.5 * (sqr(xgrid1[i] - tparams[Params.X]) / tparams[Params.SIGMA1]
                                              + sqr(ygrid1[i] - tparams[Params.Y]) / tparams[Params.SIGMA1]))
                                  * ((tparams[Params.INTENSITY] / 2.0) / (2.0 * Math.PI * tparams[Params.SIGMA1]))
                                  + tparams[Params.OFFSET1];
                }
                for(int i = 0; i < xgrid2.length; i++, index++) {
                    retVal[index] = exp(-0.5 * (sqr(xgrid2[i] - tparams[Params.X]) / tparams[Params.SIGMA2]
                                              + sqr(ygrid2[i] - tparams[Params.Y]) / tparams[Params.SIGMA2]))
                                  * ((tparams[Params.INTENSITY] / 2.0) / (2.0 * Math.PI * tparams[Params.SIGMA2]))
                                  + tparams[Params.OFFSET2];
                }
                return retVal;
            }
        };
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return useNumericalDerivatives
                ? getNumericJacobianFunction(xgrid1, ygrid1, xgrid2, ygrid2)
                : getAnalyticJacobianFunction(xgrid1, ygrid1, xgrid2, ygrid2);
    }

    protected MultivariateMatrixFunction getAnalyticJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double[][] retVal = new double[xgrid1.length + xgrid2.length][transformedPoint.length];

                int index = 0;
                for (int i = 0; i < xgrid1.length; i++, index++) {
                    double xd = (xgrid1[i] - transformedPoint[Params.X]);
                    double yd = (ygrid1[i] - transformedPoint[Params.Y]);
                    double expVal = exp(-0.5 * (sqr(xd)/transformedPoint[Params.SIGMA1] + sqr(yd)/transformedPoint[Params.SIGMA1]));
                    double oneDivPISS2 = 1 / (Math.PI * transformedPoint[Params.SIGMA1]);
                    // diff(psf, x0)
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * xd / transformedPoint[Params.SIGMA1] * expVal;
                    // diff(psf, y0)
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * yd / transformedPoint[Params.SIGMA1] * expVal;
                    // diff(psf, z0)
                    retVal[index][Params.Z] = (sqr(xd) + sqr(yd) - 2.0) * oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal
                            * ((transformedPoint[Params.Z] - calibration.c1) / sqr(calibration.d1 * (1 + sqr((transformedPoint[Params.Z] - calibration.c1) / calibration.d1))));
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off1)
                    retVal[index][Params.OFFSET1] = 2 * point[Params.OFFSET1];
                }
                for (int i = 0; i < xgrid2.length; i++, index++) {
                    double xd = (xgrid2[i] - transformedPoint[Params.X]);
                    double yd = (ygrid2[i] - transformedPoint[Params.Y]);
                    double expVal = exp(-0.5 * (sqr(xd)/transformedPoint[Params.SIGMA2] + sqr(yd)/transformedPoint[Params.SIGMA2]));
                    double oneDivPISS2 = 1 / (Math.PI * transformedPoint[Params.SIGMA2]);
                    // diff(psf, x0)
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * xd / transformedPoint[Params.SIGMA2] * expVal;
                    // diff(psf, y0)
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * yd / transformedPoint[Params.SIGMA2] * expVal;
                    // diff(psf, z0)
                    retVal[index][Params.Z] = (sqr(xd) + sqr(yd) - 2.0) * oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal
                            * ((transformedPoint[Params.Z] - calibration.c2) / sqr(calibration.d2 * (1 + sqr((transformedPoint[Params.Z] - calibration.c2) / calibration.d2))));
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off2)
                    retVal[index][Params.OFFSET2] = 2 * point[Params.OFFSET2];
                }

                return retVal;
            }
        };
    }

    protected MultivariateMatrixFunction getNumericJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
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

    @Override
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

    @Override
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
