package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import ij.IJ;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;

import java.util.Arrays;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import static java.lang.Math.abs;

/**
 * Representation of 2D elliptic Gaussian PSFModel model.
 * Specific implementation for 3D-DAOSTORM calibration file.
 */
public class EllipticGaussianDaostormPSF extends PSFModel {

    double fi, w0, d, c1, c2; // calibration data: angle, w0, d, c
    double sinfi, cosfi;

    public EllipticGaussianDaostormPSF(double fi, double w0, double d, double c1, double c2) {
        this.fi = fi;
        this.w0 = w0;
        this.d = d;
        this.c1 = c1;
        this.c2 = c2;
        this.sinfi = Math.sin(fi);
        this.cosfi = Math.cos(fi);
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        double dx = ((x - params[Params.X]) * cosfi - (y - params[Params.Y]) * sinfi);
        double dy = ((x - params[Params.X]) * sinfi + (y - params[Params.Y]) * cosfi);

        params[Params.SIGMA1] = DaostormCalibration.evalDefocus(params[Params.Z], w0, d, c1);
        params[Params.SIGMA2] = DaostormCalibration.evalDefocus(params[Params.Z], w0, d, c2);

        return params[Params.INTENSITY] / (2 * PI * params[Params.SIGMA1] * params[Params.SIGMA2]) * exp(-0.5 * (sqr(dx / params[Params.SIGMA1]) + sqr(dy / params[Params.SIGMA2]))) + params[Params.OFFSET];
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.Z] = parameters[Params.Z];
        transformed[Params.SIGMA1] = DaostormCalibration.evalDefocus(parameters[Params.Z], w0, d, c1);
        transformed[Params.SIGMA2] = DaostormCalibration.evalDefocus(parameters[Params.Z], w0, d, c2);
        transformed[Params.INTENSITY] = parameters[Params.INTENSITY] * parameters[Params.INTENSITY];
        transformed[Params.OFFSET] = parameters[Params.OFFSET] * parameters[Params.OFFSET];
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.Z] = parameters[Params.Z];
        transformed[Params.SIGMA1] = DaostormCalibration.evalDefocus(parameters[Params.Z], w0, d, c1);
        transformed[Params.SIGMA2] = DaostormCalibration.evalDefocus(parameters[Params.Z], w0, d, c2);
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.OFFSET] = sqrt(abs(parameters[Params.OFFSET]));
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            /* Derivations by Maple:
             * =====================
             * alpha = 0; x = 0; y = 0;
             * fx := (x0, y0) -> (x-x0)*cos(alpha)-(y-y0)*sin(alpha);
             * fy := (x0, y0) -> (x-x0)*sin(alpha)+(y-y0)*cos(alpha);
             *
             * w0 = 2; c1 = 150; c2 = -150; d = 400;
             * w := (z, c) -> (1/2)*w0*sqrt(1+((z-c)/d)^2);
             * sx := (z) -> w(z, c1);
             * sy := (z) -> w(z, c2);
             *
             * # b=background, I=intensity; b i I are used squared because we use the trick to take square root them before the optimization and then square them to keep them nonnegative
             * psf := b^2+A^2*exp(-1/2*(fx(x0, y0)^2/(2*sx(z0)^2)+fy(x0, y0)^2/(2*sy(z0)^2)))/(2*pi*sx(z0)*sy(z0));
             */
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double sigma1Squared = transformedPoint[Params.SIGMA1] * transformedPoint[Params.SIGMA1];
                double sigma2Squared = transformedPoint[Params.SIGMA2] * transformedPoint[Params.SIGMA2];
                double[][] retVal = new double[xgrid.length][transformedPoint.length];

                for (int i = 0; i < xgrid.length; i++) {
                    double xd = (xgrid[i] - transformedPoint[Params.X]);
                    double yd = (ygrid[i] - transformedPoint[Params.Y]);
                    double cosfiXd = cosfi * xd, cosfiYd = cosfi * yd;
                    double sinfiYd = sinfi * yd, sinfiXd = sinfi * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first) / sigma1Squared + sqr(second) / sigma2Squared));
                    double oneDivPISS2 = 1 / (PI * transformedPoint[Params.SIGMA1] * transformedPoint[Params.SIGMA2]);
                    // diff(psf, x0)
                    //double pom = (cosfiXd - cosfi * sinfiYd) / sigma1Squared + (sinfiXd + sinfi * cosfiYd) / sigma2Squared;
                    double pom1 = first*cosfi/sigma1Squared + second*sinfi/sigma2Squared;
                    retVal[i][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    //double pom2 = (cosfi * sinfiXd + cosfiYd) / sigma2Squared - (sinfi * cosfiXd - sinfiYd) / sigma1Squared;
                    double pom2 = first*sinfi/sigma1Squared + second*cosfi/sigma2Squared;
                    retVal[i][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, z0)
                    double pom4 = (transformedPoint[Params.Z] - c1) / sqr(d * (1 + sqr((transformedPoint[Params.Z] - c1) / d)));
                    double pom5 = (transformedPoint[Params.Z] - c2) / sqr(d * (1 + sqr((transformedPoint[Params.Z] - c2) / d)));
                    double pom3 = sqr(first) * pom4 + sqr(second) * pom5;
                    retVal[i][Params.Z] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom3
                                        - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom4
                                        - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom5;
                    // diff(psf, A)
                    retVal[i][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, b)
                    retVal[i][Params.OFFSET] = 2 * point[Params.OFFSET];
                }
//          IJ.log("numeric jacobian: " + Arrays.deepToString(EllipticGaussianDaostormPSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
//          IJ.log("analytic jacobian: " + Arrays.deepToString(retVal));
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
        steps[Params.Z] = 10;
        steps[Params.INTENSITY] = 3000;
        steps[Params.OFFSET] = 10;
        return steps;
    }

    @Override
    public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = subImage.detectorX;
        guess[Params.Y] = subImage.detectorY;
        guess[Params.Z] = 0;
        guess[Params.SIGMA1] = DaostormCalibration.evalDefocus(guess[Params.Z], w0, d, c1);
        guess[Params.SIGMA2] = DaostormCalibration.evalDefocus(guess[Params.Z], w0, d, c2);
        guess[Params.INTENSITY] = (subImage.getMax() - subImage.getMin()) * 2 * PI * guess[Params.SIGMA1] * guess[Params.SIGMA2];
        guess[Params.OFFSET] = subImage.getMin();
        return guess;
    }
    
    @Override 
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits) {
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.Z, Params.SIGMA1, Params.SIGMA2, Params.INTENSITY, Params.OFFSET, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public double getDoF() {
        return 6;
    }
}
