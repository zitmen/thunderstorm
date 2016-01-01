package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import static java.lang.Math.abs;
import java.util.Arrays;

/**
 * Representation of 2D elliptic Gaussian PSFModel model.
 */
public class EllipticGaussianPSF extends PSFModel {

    DefocusCalibration calibration = null;
    double defaultSigma = 1.6;
    double fi, sinfi, cosfi;

    public EllipticGaussianPSF(double defaultSigma, double angle) {
        this.defaultSigma = defaultSigma;
        this.fi = angle;
        this.sinfi = Math.sin(fi);
        this.cosfi = Math.cos(fi);
    }

    public EllipticGaussianPSF(DefocusCalibration calibration) {
        this.calibration = calibration;
        this.fi = calibration.getAngle();
        this.sinfi = Math.sin(fi);
        this.cosfi = Math.cos(fi);
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        double dx = ((x - params[Params.X]) * cosfi - (y - params[Params.Y]) * sinfi);
        double dy = ((x - params[Params.X]) * sinfi + (y - params[Params.Y]) * cosfi);

        if (calibration != null) {
            params[Params.SIGMA1] = calibration.getSigma1Squared(params[Params.Z]);
            params[Params.SIGMA2] = calibration.getSigma2Squared(params[Params.Z]);
        }

        return params[Params.INTENSITY] * exp(-0.5 * (sqr(dx)/params[Params.SIGMA1] + sqr(dy)/params[Params.SIGMA2])) + params[Params.OFFSET];
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        if (calibration != null) {
            transformed[Params.SIGMA1] = calibration.getSigma1Squared(parameters[Params.Z]);
            transformed[Params.SIGMA2] = calibration.getSigma2Squared(parameters[Params.Z]);
        } else {
            transformed[Params.SIGMA1] = sqr(parameters[Params.SIGMA1]);
            transformed[Params.SIGMA2] = sqr(parameters[Params.SIGMA2]);
        }
        transformed[Params.INTENSITY] = sqr(parameters[Params.INTENSITY]);
        transformed[Params.OFFSET] = sqr(parameters[Params.OFFSET]);
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        if (calibration != null) {
            transformed[Params.SIGMA1] = calibration.getSigma1Squared(parameters[Params.Z]);
            transformed[Params.SIGMA2] = calibration.getSigma2Squared(parameters[Params.Z]);
        } else {
            transformed[Params.SIGMA1] = sqrt(abs(parameters[Params.SIGMA1]));
            transformed[Params.SIGMA2] = sqrt(abs(parameters[Params.SIGMA2]));
        }
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.OFFSET] = sqrt(abs(parameters[Params.OFFSET]));
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid, final double[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double[][] retVal = new double[xgrid.length][transformedPoint.length];

                for (int i = 0; i < xgrid.length; i++) {
                    double xd = (xgrid[i] - transformedPoint[Params.X]);
                    double yd = (ygrid[i] - transformedPoint[Params.Y]);
                    double cosfiXd = cosfi * xd, cosfiYd = cosfi * yd;
                    double sinfiYd = sinfi * yd, sinfiXd = sinfi * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first)/transformedPoint[Params.SIGMA1] + sqr(second)/transformedPoint[Params.SIGMA2]));
                    // diff(psf, x0)
                    double pom1 =  first*cosfi/transformedPoint[Params.SIGMA1] + second*sinfi/transformedPoint[Params.SIGMA2];
                    retVal[i][Params.X] = transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    double pom2 = -first*sinfi/transformedPoint[Params.SIGMA1] + second*cosfi/transformedPoint[Params.SIGMA2];
                    retVal[i][Params.Y] = transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, I)
                    retVal[i][Params.INTENSITY] = 2*point[Params.INTENSITY] * expVal;
                    if (calibration != null) {
                        // diff(psf, z0)
                        double pom3 = calibration.dwx2(transformedPoint[PSFModel.Params.Z]) / 2.0 * sqr(first /transformedPoint[Params.SIGMA1])
                                    + calibration.dwy2(transformedPoint[PSFModel.Params.Z]) / 2.0 * sqr(second/transformedPoint[Params.SIGMA2]);
                        retVal[i][Params.Z] = transformedPoint[Params.INTENSITY] * expVal * pom3;
                    } else {
                        // diff(psf, sigma1)
                        retVal[i][Params.SIGMA1] = transformedPoint[Params.INTENSITY] * expVal * sqr(first)  / sqr(transformedPoint[Params.SIGMA1]);
                        // diff(psf, sigma2)
                        retVal[i][Params.SIGMA2] = transformedPoint[Params.INTENSITY] * expVal * sqr(second) / sqr(transformedPoint[Params.SIGMA2]);
                    }
                    // diff(psf, off)
                    retVal[i][Params.OFFSET] = 2 * point[Params.OFFSET];
                }
                //IJ.log("numeric jacobian: " + Arrays.deepToString(retVal2));
                //IJ.log("analytic jacobian: " + Arrays.deepToString(retVal));
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
        steps[Params.Z] = 100;
        steps[Params.INTENSITY] = 3000;
        steps[Params.SIGMA1] = 0.1;
        steps[Params.SIGMA2] = 0.1;
        steps[Params.OFFSET] = 10;
        return steps;
    }

    @Override
    public double[] getInitialParams(SubImage subImage) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = subImage.detectorX;
        guess[Params.Y] = subImage.detectorY;
        guess[Params.Z] = 0;
        guess[Params.INTENSITY] = subImage.getMax() - subImage.getMin();
        if (calibration != null) {
            guess[Params.SIGMA1] = calibration.getSigma1Squared(guess[Params.Z]);
            guess[Params.SIGMA2] = calibration.getSigma2Squared(guess[Params.Z]);
        } else {
            guess[Params.SIGMA1] = sqr(defaultSigma);
            guess[Params.SIGMA2] = sqr(defaultSigma);
        }
        guess[Params.OFFSET] = subImage.getMin();
        return guess;
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        if (afterFitting) {
            params[Params.SIGMA1] = sqrt(params[Params.SIGMA1]);
            params[Params.SIGMA2] = sqrt(params[Params.SIGMA2]);
            params[Params.INTENSITY] = params[Params.INTENSITY] * 2 * PI * params[Params.SIGMA1] * params[Params.SIGMA2];
        }
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.Z, Params.SIGMA1, Params.SIGMA2, Params.INTENSITY, Params.OFFSET, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public double getDoF() {
        return 5;
    }
}
