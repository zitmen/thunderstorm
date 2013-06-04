package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
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

  /**
   *
   */
  public double defaultSigma;
  private static final String[] parameterNames = {"x", "y", "intensity", "sigma", "background"};

  public SymmetricGaussianPSF(double defaultSigma) {
    this.defaultSigma = defaultSigma;
  }

  /**
   *
   * @return
   */
  @Override
  public String[] getParamNames() {
    return parameterNames;
  }

  @Override
  public double getExpectedValue(double[] params, int x, int y) {
    double twoSigmaSquared = params[3] * params[3] * 2;
    return params[4] * params[4] + params[2] / (twoSigmaSquared * PI)
            * exp(-((x - params[0]) * (x - params[0]) + (y - params[1]) * (y - params[1])) / twoSigmaSquared);
  }

  @Override
  public double getValue(double[] params, double x, double y) {
    double twoSigmaSquared = params[3] * params[3] * 2;
    return params[4] + params[2] / (twoSigmaSquared * PI)
            * exp(-((x - params[0]) * (x - params[0]) + (y - params[1]) * (y - params[1])) / twoSigmaSquared);
  }

  @Override
  public double[] transformParameters(double[] parameters) {
    double[] transformed = new double[5];
    transformed[0] = parameters[0];
    transformed[1] = parameters[1];
    transformed[2] = parameters[2] * parameters[2];
    transformed[3] = parameters[3] * parameters[3];
    transformed[4] = parameters[4] * parameters[4];
    return transformed;
  }

  @Override
  public double[] transformParametersInverse(double[] parameters) {
    double[] transformed = new double[5];
    transformed[0] = parameters[0];
    transformed[1] = parameters[1];
    transformed[2] = sqrt(abs(parameters[2]));
    transformed[3] = sqrt(abs(parameters[3]));
    transformed[4] = sqrt(abs(parameters[4]));
    return transformed;
  }

  @Override
  public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
    return new MultivariateMatrixFunction() {
      @Override
      //derivations by wolfram alpha:
      //d(b^2 + ((J*J)/2/PI/(s*s)/(s*s)) * e^( -( ((x0-x)^2)/(2*s*s*s*s) + (((y0-y)^2)/(2*s*s*s*s)))))/dx
      public double[][] value(double[] point) throws IllegalArgumentException {
        double[] transformedPoint = transformParameters(point);
        double sigma = transformedPoint[3];
        double sigmaSquared = sigma * sigma;
        double[][] retVal = new double[xgrid.length][transformedPoint.length];

        for (int i = 0; i < xgrid.length; i++) {
          //d()/dIntensity
          double xd = (xgrid[i] - transformedPoint[0]);
          double yd = (ygrid[i] - transformedPoint[1]);
          double upper = -(xd * xd + yd * yd) / (2 * sigmaSquared);
          double expVal = exp(upper);
          double expValDivPISigmaSquared = expVal / (sigmaSquared * PI);
          double expValDivPISigmaPowEight = expValDivPISigmaSquared / sigmaSquared;
          retVal[i][0] = point[2] * expValDivPISigmaSquared;
          //d()/dx
          retVal[i][1] = transformedPoint[2] * xd * expValDivPISigmaPowEight * 0.5;
          //d()/dy
          retVal[i][2] = transformedPoint[2] * yd * expValDivPISigmaPowEight * 0.5;
          //d()/dsigma
          retVal[i][3] = transformedPoint[2] * expValDivPISigmaPowEight / point[3] * (xd * xd + yd * yd - 2 * sigmaSquared);
          //d()/dbkg
          retVal[i][4] = 2 * point[4];
        }
        return retVal;
      }
    };
  }

  @Override
  public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
    double[] retValue = new double[5];
    retValue[0] = subImage.detectorX;
    retValue[1] = subImage.detectorY;
    retValue[2] = (subImage.getMax() - subImage.getMin()) * 2 * PI * defaultSigma * defaultSigma;
    retValue[3] = defaultSigma;
    retValue[4] = subImage.getMin();
    return retValue;
  }
}
