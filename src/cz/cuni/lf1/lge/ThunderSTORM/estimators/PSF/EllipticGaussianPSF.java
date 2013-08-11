package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.*;
import static java.lang.Math.abs;
import java.util.Arrays;

/**
 * Representation of 2D elliptic Gaussian PSFModel model.
 */
public class EllipticGaussianPSF extends PSFModel {

    double defaultSigma;
    double fi; //angle
    double sinfi, cosfi;
    
    public EllipticGaussianPSF(double defaultSigma, double fi) {
        this.defaultSigma = defaultSigma;
        this.fi = fi;
        this.sinfi = Math.sin(fi);
        this.cosfi = Math.cos(fi);
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        double dx = ((x - params[Params.X]) * cosfi - (y - params[Params.Y]) * sinfi);
        double dy = ((x - params[Params.X]) * sinfi + (y - params[Params.Y]) * cosfi);

        return params[Params.INTENSITY] / (2 * PI * params[Params.SIGMA1] * params[Params.SIGMA2]) * exp(-0.5 * (sqr(dx / params[Params.SIGMA1]) + sqr(dy / params[Params.SIGMA2]))) + params[Params.BACKGROUND];
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double[] transformed = new double[parameters.length];
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = parameters[Params.INTENSITY] * parameters[Params.INTENSITY];
        transformed[Params.SIGMA1] = parameters[Params.SIGMA1] * parameters[Params.SIGMA1];
        transformed[Params.SIGMA2] = parameters[Params.SIGMA2] * parameters[Params.SIGMA2];
        transformed[Params.BACKGROUND] = parameters[Params.BACKGROUND] * parameters[Params.BACKGROUND];
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double[] transformed = new double[parameters.length];
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.SIGMA1] = sqrt(abs(parameters[Params.SIGMA1]));
        transformed[Params.SIGMA2] = sqrt(abs(parameters[Params.SIGMA2]));
        transformed[Params.BACKGROUND] = sqrt(abs(parameters[Params.BACKGROUND]));
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            //derivations by wolfram alpha:
            //d(b^2 + ((J*J)/(2*PI*(s1*s1)*(s2*s2))) * e^( -( (((x0-x)*cos(f)-(y0-y)*sin(f))^2)/(2*s1*s1*s1*s1) + ((((x0-x)*sin(f)+(y0-y)*cos(f))^2)/(2*s2*s2*s2*s2)))))/dJ
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
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
                    retVal[i][Params.BACKGROUND] = 2 * point[Params.BACKGROUND];
                }
//          IJ.log("numeric jacobian: " + Arrays.deepToString(EllipticGaussianPSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
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
        steps[Params.SIGMA] = 0.1;
        steps[Params.BACKGROUND] = 10;
        return steps;
    }

    @Override
    public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
        double[] guess = new double[Params.PARAMS_LENGTH];
        Arrays.fill(guess, 0);
        guess[Params.X] = subImage.detectorX;
        guess[Params.Y] = subImage.detectorY;
        guess[Params.INTENSITY] = (subImage.getMax() - subImage.getMin()) * 2 * PI * defaultSigma * defaultSigma;
        guess[Params.SIGMA1] = defaultSigma;
        guess[Params.SIGMA2] = defaultSigma;
        guess[Params.BACKGROUND] = subImage.getMin();
        return guess;
    }
    
    @Override
    public PSFInstance newInstanceFromParams(double[] params) {
        return new PSFInstance(new Params(new int[] { Params.X, Params.Y, Params.SIGMA1, Params.SIGMA2, Params.INTENSITY, Params.BACKGROUND }, params, true));
    }
}
