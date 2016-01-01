package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import java.util.Arrays;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.sqrt;

/**
 * Representation of 2D symmetric Gaussian PSFModel model.
 *
 * <strong>Note that this class will be completely changed in a future
 * relase.</strong>
 */
public class SymmetricGaussianPSF extends PSFModel {

    public double defaultSigma;

    public SymmetricGaussianPSF(double defaultSigma) {
        this.defaultSigma = defaultSigma;
    }

    @Override
    public double getValue(double[] params, double x, double y) {
        double twoSigmaSquared = params[Params.SIGMA] * params[Params.SIGMA] * 2;
        return params[Params.OFFSET] + params[Params.INTENSITY] / (twoSigmaSquared * PI)
                * exp(-((x - params[Params.X]) * (x - params[Params.X]) + (y - params[Params.Y]) * (y - params[Params.Y])) / twoSigmaSquared);
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = parameters[Params.INTENSITY] * parameters[Params.INTENSITY];
        transformed[Params.SIGMA] = parameters[Params.SIGMA] * parameters[Params.SIGMA];
        transformed[Params.OFFSET] = parameters[Params.OFFSET] * parameters[Params.OFFSET];
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.X] = parameters[Params.X];
        transformed[Params.Y] = parameters[Params.Y];
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.SIGMA] = sqrt(abs(parameters[Params.SIGMA]));
        transformed[Params.OFFSET] = sqrt(abs(parameters[Params.OFFSET]));
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final double[] xgrid, final double[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            //derivations by wolfram alpha:
            //d(b^2 + ((J*J)/2/PI/(s*s)/(s*s)) * e^( -( ((x0-x)^2)/(2*s*s*s*s) + (((y0-y)^2)/(2*s*s*s*s)))))/dx
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double sigma = transformedPoint[Params.SIGMA];
                double sigmaSquared = sigma * sigma;
                double[][] retVal = new double[xgrid.length][transformedPoint.length];

                for (int i = 0; i < xgrid.length; i++) {
                    //d()/dIntensity
                    double xd = (xgrid[i] - transformedPoint[Params.X]);
                    double yd = (ygrid[i] - transformedPoint[Params.Y]);
                    double upper = -(xd * xd + yd * yd) / (2 * sigmaSquared);
                    double expVal = exp(upper);
                    double expValDivPISigmaSquared = expVal / (sigmaSquared * PI);
                    double expValDivPISigmaPowEight = expValDivPISigmaSquared / sigmaSquared;
                    retVal[i][Params.INTENSITY] = point[Params.INTENSITY] * expValDivPISigmaSquared;
                    //d()/dx
                    retVal[i][Params.X] = transformedPoint[Params.INTENSITY] * xd * expValDivPISigmaPowEight * 0.5;
                    //d()/dy
                    retVal[i][Params.Y] = transformedPoint[Params.INTENSITY] * yd * expValDivPISigmaPowEight * 0.5;
                    //d()/dsigma
                    retVal[i][Params.SIGMA] = transformedPoint[Params.INTENSITY] * expValDivPISigmaPowEight / point[Params.SIGMA] * (xd * xd + yd * yd - 2 * sigmaSquared);
                    //d()/dbkg
                    retVal[i][Params.OFFSET] = 2 * point[Params.OFFSET];
                }
                //IJ.log("numeric jacobian: " + Arrays.deepToString(SymmetricGaussianPSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
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
    public Molecule newInstanceFromParams(double[] params, Units subImageUnits, boolean afterFitting) {
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.SIGMA, Params.INTENSITY, Params.OFFSET, Params.BACKGROUND }, params, true));
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
