package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;
import static java.lang.Math.abs;
import java.util.Arrays;

/**
 * Representation of 2D elliptic Gaussian PSFModel model.
 */
public class EllipticGaussianWAnglePSF extends PSFModel {

    double defaultSigma;
    double defaultFi; //angle

    public EllipticGaussianWAnglePSF(double defaultSigma, double fi) {
        this.defaultSigma = defaultSigma;
        this.defaultFi = fi;
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        double sinfi = Math.sin(params[Params.ANGLE]);
        double cosfi = Math.cos(params[Params.ANGLE]);
        double dx = ((x - params[Params.X]) * cosfi - (y - params[Params.Y]) * sinfi);
        double dy = ((x - params[Params.X]) * sinfi + (y - params[Params.Y]) * cosfi);

        return params[Params.INTENSITY] / (2 * PI * params[Params.SIGMA1] * params[Params.SIGMA2]) * exp(-0.5 * (sqr(dx / params[Params.SIGMA1]) + sqr(dy / params[Params.SIGMA2]))) + params[Params.OFFSET];
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = parameters[Params.INTENSITY] * parameters[Params.INTENSITY];
        transformed[Params.SIGMA1] = parameters[Params.SIGMA1] * parameters[Params.SIGMA1];
        transformed[Params.SIGMA2] = parameters[Params.SIGMA2] * parameters[Params.SIGMA2];
        transformed[Params.OFFSET] = parameters[Params.OFFSET] * parameters[Params.OFFSET];
        transformed[Params.ANGLE] = parameters[Params.ANGLE];
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.SIGMA1] = sqrt(abs(parameters[Params.SIGMA1]));
        transformed[Params.SIGMA2] = sqrt(abs(parameters[Params.SIGMA2]));
        transformed[Params.OFFSET] = sqrt(abs(parameters[Params.OFFSET]));
        transformed[Params.ANGLE] = parameters[Params.ANGLE];
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid, final double[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            //derivations by wolfram alpha:
            //d(b^2 + ((J*J)/(2*PI*(s1*s1)*(s2*s2))) * e^( -( (((x0-x)*cos(f)-(y0-y)*sin(f))^2)/(2*s1*s1*s1*s1) + ((((x0-x)*sin(f)+(y0-y)*cos(f))^2)/(2*s2*s2*s2*s2)))))/dJ
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double sinfi = Math.sin(transformedPoint[Params.ANGLE]);
                double cosfi = Math.cos(transformedPoint[Params.ANGLE]);
                double sigma1Squared = transformedPoint[Params.SIGMA1] * transformedPoint[Params.SIGMA1];
                double sigma2Squared = transformedPoint[Params.SIGMA2] * transformedPoint[Params.SIGMA2];
                double[][] retVal = new double[xgrid.length][transformedPoint.length];

                for (int i = 0; i < xgrid.length; i++) {
                    double xd = (xgrid[i] - transformedPoint[Params.X]);
                    double yd = (ygrid[i] - transformedPoint[Params.Y]);
                    double cosfiXd = cosfi * xd;
                    double cosfiYd = cosfi * yd;
                    double sinfiYd = sinfi * yd;
                    double sinfiXd = sinfi * xd;
                    double first = cosfiXd - sinfiYd;
                    double second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first) / sigma1Squared + sqr(second) / sigma2Squared));
                    double oneDivPISS2 = 1 / (PI * transformedPoint[Params.SIGMA1] * transformedPoint[Params.SIGMA2]);
                    //d()/dx
                    double pom = (cosfiXd - cosfi * sinfiYd) / sigma1Squared + (sinfiXd + sinfi * cosfiYd) / sigma2Squared;
                    retVal[i][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom * expVal;
                    //d()/dy
                    double pom2 = (cosfi * sinfiXd + cosfiYd) / sigma2Squared - (sinfi * cosfiXd - sinfiYd) / sigma1Squared;
                    retVal[i][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    //d()/dIntensity
                    retVal[i][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    //d()/dsigma1
                    retVal[i][Params.SIGMA1] = transformedPoint[Params.INTENSITY] * expVal * oneDivPISS2 / point[Params.SIGMA1] * (-1 + sqr(first) / sigma1Squared);
                    //d()/dsigma2
                    retVal[i][Params.SIGMA2] = transformedPoint[Params.INTENSITY] * expVal * oneDivPISS2 / point[Params.SIGMA2] * (-1 + sqr(second) / sigma2Squared);
                    //d()/dbkg
                    retVal[i][Params.OFFSET] = 2 * point[Params.OFFSET];
                    //d()/dfi
                    double pom3 = -(cosfiXd - sinfiYd) * (-sinfiXd - cosfiYd) / sigma1Squared - (sinfiXd + cosfiYd) * (cosfiXd - sinfiYd) / sigma2Squared;
                    retVal[i][Params.ANGLE] = 0.5 * transformedPoint[Params.INTENSITY] * pom3 * expVal * oneDivPISS2;
                }
//          IJ.log("numeric jacobian: " + Arrays.deepToString(EllipticGaussianWAnglePSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
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
        steps[Params.INTENSITY] = 3000;
        steps[Params.SIGMA1] = 0.1;
        steps[Params.SIGMA2] = 0.1;
        steps[Params.OFFSET] = 10;
        steps[Params.ANGLE] = 0.1;
        return steps;
    }

    @Override
    public double[] getInitialParams(SubImage subImage) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = subImage.detectorX;
        guess[Params.Y] = subImage.detectorY;
        guess[Params.INTENSITY] = (subImage.getMax() - subImage.getMin()) * 2 * PI * defaultSigma * defaultSigma;
        guess[Params.SIGMA1] = defaultSigma;
        guess[Params.SIGMA2] = defaultSigma;
        guess[Params.OFFSET] = subImage.getMin();
        guess[Params.ANGLE] = defaultFi;
        return guess;
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        Molecule mol =  new Molecule(new Params(new int[] { Params.X, Params.Y, Params.SIGMA1, Params.SIGMA2, Params.ANGLE, Params.INTENSITY, Params.OFFSET, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public double getDoF() {
        return 7;
    }
}
