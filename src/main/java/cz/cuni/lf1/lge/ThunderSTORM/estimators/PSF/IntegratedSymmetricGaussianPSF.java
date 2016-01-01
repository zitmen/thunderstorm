package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import java.util.Arrays;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

/**
 * Representation of 2D symmetric Gaussian PSF model.
 *
 */
public class IntegratedSymmetricGaussianPSF extends PSFModel {

    public double defaultSigma;

    public IntegratedSymmetricGaussianPSF(double defaultSigma) {
        this.defaultSigma = defaultSigma;
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        //integration by mathematica 9
        //Integrate[ b^2 + J^2/(2*Pi*s^2) *E^(-1/2*((x - x0)^2/(s^2) + (y - y0)^2/(s^2))), {x, ax - 1/2, ax + 1/2}, {y, ay - 1/2, ay + 1/2}]
        double sqrt2s = sqrt(2) * params[Params.SIGMA];
        double dx = x - params[Params.X];
        double dy = y - params[Params.Y];
        double errdifx = erf((dx + 0.5) / sqrt2s) - erf((dx - 0.5) / sqrt2s);
        double errdify = erf((dy + 0.5) / sqrt2s) - erf((dy - 0.5) / sqrt2s);
        return params[Params.OFFSET] + 0.25 * params[Params.INTENSITY] * errdifx * errdify;
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double[] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = parameters[Params.INTENSITY] * parameters[Params.INTENSITY];
        transformed[Params.SIGMA] = parameters[Params.SIGMA];
        transformed[Params.OFFSET] = parameters[Params.OFFSET] * parameters[Params.OFFSET];
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double[] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.SIGMA] = parameters[Params.SIGMA];
        transformed[Params.OFFSET] = sqrt(abs(parameters[Params.OFFSET]));
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid, final double[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            //gradients by mathematica 9
            //Grad[b^2 + (1/4)*J^2*(Erf[(1/2 + ax - x0)/(Sqrt[2]*s)] - Erf[(-(1/2) + ax - x0)/(Sqrt[2]*s)])* (Erf[(1/2 + ay - y0)/(Sqrt[2]*s)] - Erf[(-(1/2) + ay - y0)/(Sqrt[2]*s)]), {b, J, s, x0, y0}]
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double sigma = transformedPoint[Params.SIGMA];
                double sigmaSquared = sigma * sigma;
                double sqrt2s = sqrt(2) * sigma;
                double[][] retVal = new double[xgrid.length][transformedPoint.length];

                assert (isRegular(xgrid, ygrid));
                int edge = (int) sqrt(xgrid.length);

                double[] errDiffXs = new double[edge];
                double[] expDiffSXs = new double[edge];
                double[] expDiffXs = new double[edge];
                for(int i = 0; i < errDiffXs.length; i++) {
                    double dx = xgrid[i] - transformedPoint[Params.X];
                    double dxPlusHalf = dx + 0.5;
                    double dxMinusHalf = dx - 0.5;
                    errDiffXs[i] = erf((dxPlusHalf) / sqrt2s) - erf((dxMinusHalf) / sqrt2s);

                    double expplus = exp(-sqr(dxPlusHalf) / (2 * sigmaSquared)) * sqrt(2 / PI);
                    double expminus = exp(-sqr(dxMinusHalf) / (2 * sigmaSquared)) * sqrt(2 / PI);
                    expDiffSXs[i] = (expminus * dxMinusHalf - expplus * dxPlusHalf) / sigmaSquared;
                    expDiffXs[i] = (expminus - expplus) / sigma;
                }

                int idx = 0;
                for(int i = 0; i < edge; i++) {
                    double dy = ygrid[i * edge] - transformedPoint[Params.Y];
                    double dyPlusHalf = dy + 0.5;
                    double dyMinusHalf = dy - 0.5;
                    double errDiffY = erf((dyPlusHalf) / sqrt2s) - erf((dyMinusHalf) / sqrt2s);

                    double expplus = exp(-sqr(dyPlusHalf) / (2 * sigmaSquared)) * sqrt(2 / PI);
                    double expminus = exp(-sqr(dyMinusHalf) / (2 * sigmaSquared)) * sqrt(2 / PI);
                    double expDiffSY = (expminus * dyMinusHalf - expplus * dyPlusHalf) / sigmaSquared;
                    double expDiffY = (expminus - expplus) / sigma;
                    for(int j = 0; j < edge; j++) {
                        double errDiffX = errDiffXs[j];
                        retVal[idx][Params.INTENSITY] = 0.5 * point[Params.INTENSITY] * errDiffX * errDiffY;
                        retVal[idx][Params.SIGMA] = 0.25 * transformedPoint[Params.INTENSITY] * (errDiffX * expDiffSY + errDiffY * expDiffSXs[j]);
                        retVal[idx][Params.X] = 0.25 * transformedPoint[Params.INTENSITY] * errDiffY * expDiffXs[j];
                        retVal[idx][Params.Y] = 0.25 * transformedPoint[Params.INTENSITY] * errDiffX * expDiffY;
                        retVal[idx][Params.OFFSET] = 2 * point[Params.OFFSET];
                        idx++;
                    }
                }

//                IJ.log("numeric jacobian: " + Arrays.deepToString(IntegratedSymmetricGaussianPSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
//                IJ.log("analytic jacobian: " + Arrays.deepToString(retVal));
                return retVal;
            }
        };
    }

    /**
     * Value function overriden for speed. When calculating for the whole
     * subimage, some values can be reused. But can only be used for a square
     * grid where xgrid values are same in each column and ygrid values are the
     * same in each row.
     *
     * @param xgrid
     * @param ygrid
     * @return
     */
    @Override
    public MultivariateVectorFunction getValueFunction(final double[] xgrid, final double[] ygrid) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(final double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double sigma = transformedPoint[Params.SIGMA];
                double sqrt2s = sqrt(2) * sigma;
                double[] retVal = new double[xgrid.length];

                assert (isRegular(xgrid, ygrid));
                int edge = (int) sqrt(xgrid.length);

                double[] errDiffXs = new double[edge];
                for(int i = 0; i < errDiffXs.length; i++) {
                    double dx = xgrid[i] - transformedPoint[Params.X];
                    double dxPlusHalf = dx + 0.5;
                    double dxMinusHalf = dx - 0.5;
                    errDiffXs[i] = erf((dxPlusHalf) / sqrt2s) - erf((dxMinusHalf) / sqrt2s);
                }

                int idx = 0;
                for(int i = 0; i < edge; i++) {
                    double dy = ygrid[i * edge] - transformedPoint[Params.Y];
                    double dyPlusHalf = dy + 0.5;
                    double dyMinusHalf = dy - 0.5;
                    double errDiffY = erf((dyPlusHalf) / sqrt2s) - erf((dyMinusHalf) / sqrt2s);

                    for(int j = 0; j < edge; j++) {
                        double errDiffX = errDiffXs[j];
                        retVal[idx] = transformedPoint[Params.OFFSET] + 0.25 * transformedPoint[Params.INTENSITY] * errDiffX * errDiffY;
                        idx++;
                    }
                }
                return retVal;
            }
        };
    }

    @Override
    public double[] getInitialSimplex() {
        double[] steps = new double[Params.PARAMS_LENGTH];
        Arrays.fill(steps, 0.001);  // cannot be zero!
        steps[Params.X] = 1;
        steps[Params.Y] = 1;
        steps[Params.INTENSITY] = 3000;
        steps[Params.SIGMA] = 0.1;
        steps[Params.OFFSET] = 10;
        return steps;
    }

    @Override
    public double[] getInitialParams(SubImage subImage) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = subImage.detectorX;
        guess[Params.Y] = subImage.detectorY;
        guess[Params.INTENSITY] = (subImage.getMax() - subImage.getMin()) * 2 * PI * defaultSigma * defaultSigma;
        guess[Params.SIGMA] = defaultSigma;
        guess[Params.OFFSET] = subImage.getMin();
        return guess;
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        params[Params.SIGMA] = abs(params[Params.SIGMA]);
        Molecule mol = new Molecule(new Params(new int[]{Params.X, Params.Y, Params.SIGMA, Params.INTENSITY, Params.OFFSET, Params.BACKGROUND}, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    // from Chebyshev fitting formula for erf(z) from Numerical Recipes, 6.2
    public static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp(-z * z - 1.26551223
                + t * (1.00002368
                + t * (0.37409196
                + t * (0.09678418
                + t * (-0.18628806
                + t * (0.27886807
                + t * (-1.13520398
                + t * (1.48851587
                + t * (-0.82215223
                + t * (0.17087277))))))))));
        if(z >= 0) {
            return ans;
        } else {
            return -ans;
        }
    }

    // fractional error less than x.xx * 10 ^ -4.
    // Algorithm 26.2.17 in Abromowitz and Stegun, Handbook of Mathematical.
    public static double erf2(double z) {
        double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
        double poly = t * (0.3480242 + t * (-0.0958798 + t * (0.7478556)));
        double ans = 1.0 - poly * Math.exp(-z * z);
        if(z >= 0) {
            return ans;
        } else {
            return -ans;
        }
    }

    /**
     * Checks whether the grid is regular (xgrid values are the same in each row
     * and ygrid values are the same in each column).
     * <p/>
     * Example of regular grid:
     * <pre>
     * xgrid:
     * 0 0 0
     * 1 1 1
     * 2 2 2
     *
     * ygrid:
     * 0 1 2
     * 0 1 2
     * 0 1 2
     * </pre>
     *
     */
    private static boolean isRegular(double[] xgrid, double[] ygrid) {
        int edge = (int) sqrt(xgrid.length);
        if(edge * edge != xgrid.length) {
            return false;
        }
        if(xgrid.length != ygrid.length) {
            return false;
        }

        for(int i = 0; i < edge; i++) {
            double prevValue = xgrid[i * edge];
            for(int j = 0; j < 0; j++) {
                if(xgrid[i * edge + j] != prevValue) {
                    return false;
                }
            }
        }

        for(int i = 0; i < edge; i++) {
            double prevValue = ygrid[i];
            for(int j = 0; j < 0; j++) {
                if(ygrid[i + j * edge] != prevValue) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public double getDoF() {
        return 5;
    }
}
