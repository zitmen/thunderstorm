package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.*;
import static java.lang.Math.abs;

/**
 * Representation of 2D elliptic Gaussian PSFModel model.
 *
 */
public class EllipticGaussianWAnglePSF extends PSFModel {

  /**
   *
   */
  double defaultSigma;
  double defaultFi; //angle
  private static final String[] parameterNames = {PSFInstance.X, PSFInstance.Y, "intensity", PSFInstance.SIGMA, PSFInstance.SIGMA2, "background", "angle"};

  public EllipticGaussianWAnglePSF(double defaultSigma, double fi) {
    this.defaultSigma = defaultSigma;
    this.defaultFi = fi;
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
  public double getValue(double[] params, double x, double y) {
    double sinfi = Math.sin(params[6]);
    double cosfi = Math.cos(params[6]);
    double dx = ((x - params[0]) * cosfi - (y - params[1]) * sinfi);
    double dy = ((x - params[0]) * sinfi + (y - params[1]) * cosfi);

    return params[2] / (2 * PI * params[3] * params[4]) * exp(-0.5 * (sqr(dx / params[3]) + sqr(dy / params[4]))) + params[5];
  }

  @Override
  public double[] transformParameters(double[] parameters) {
    double[] transformed = new double[7];
    transformed[0] = parameters[0];
    transformed[1] = parameters[1];
    transformed[2] = parameters[2] * parameters[2];
    transformed[3] = parameters[3] * parameters[3];
    transformed[4] = parameters[4] * parameters[4];
    transformed[5] = parameters[5] * parameters[5];
    transformed[6] = parameters[6];
    return transformed;
  }

  @Override
  public double[] transformParametersInverse(double[] parameters) {
    double[] transformed = new double[7];
    transformed[0] = parameters[0];
    transformed[1] = parameters[1];
    transformed[2] = sqrt(abs(parameters[2]));
    transformed[3] = sqrt(abs(parameters[3]));
    transformed[4] = sqrt(abs(parameters[4]));
    transformed[5] = sqrt(abs(parameters[5]));
    transformed[6] = parameters[6];
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
        double sinfi = Math.sin(transformedPoint[6]);
        double cosfi = Math.cos(transformedPoint[6]);
        double sigmaSquared = transformedPoint[3] * transformedPoint[3];
        double sigma2Squared = transformedPoint[4] * transformedPoint[4];
        double[][] retVal = new double[xgrid.length][transformedPoint.length];

        for (int i = 0; i < xgrid.length; i++) {
          double xd = (xgrid[i] - transformedPoint[0]);
          double yd = (ygrid[i] - transformedPoint[1]);
          double cosfiXd = cosfi * xd;
          double cosfiYd = cosfi * yd;
          double sinfiYd = sinfi * yd;
          double sinfiXd = sinfi * xd;
          double first = cosfiXd - sinfiYd;
          double second = sinfiXd + cosfiYd;
          double expVal = exp(-0.5 * (sqr(first) / sigmaSquared + sqr(second) / sigma2Squared));
          double oneDivPISS2 = 1 / (PI * transformedPoint[3] * transformedPoint[4]);
          //d()/dx
          double pom = (cosfiXd - cosfi * sinfiYd) / sigmaSquared + (sinfiXd + sinfi * cosfiYd) / sigma2Squared;
          retVal[i][0] = oneDivPISS2 * 0.5 * transformedPoint[2] * pom * expVal;
          //d()/dy
          double pom2 = (cosfi * sinfiXd + cosfiYd) / sigma2Squared - (sinfi * cosfiXd - sinfiYd) / sigmaSquared;
          retVal[i][1] = oneDivPISS2 * 0.5 * transformedPoint[2] * pom2 * expVal;
          //d()/dIntensity
          retVal[i][2] = point[2] * expVal * oneDivPISS2;
          //d()/dsigma1
          retVal[i][3] = transformedPoint[2] * expVal * oneDivPISS2 / point[3] * (-1 + sqr(first) / sigmaSquared);
          //d()/dsigma2
          retVal[i][4] = transformedPoint[2] * expVal * oneDivPISS2 / point[4] * (-1 + sqr(second) / sigma2Squared);
          //d()/dbkg
          retVal[i][5] = 2 * point[5];
          //d()/dfi
          double pom3 = -(cosfiXd - sinfiYd) * (-sinfiXd - cosfiYd) / sigmaSquared - (sinfiXd + cosfiYd) * (cosfiXd - sinfiYd) / sigma2Squared;
          retVal[i][6] = 0.5 * transformedPoint[2] * pom3 * expVal * oneDivPISS2;
        }
//          IJ.log("numeric jacobian: " + Arrays.deepToString(EllipticGaussianWAnglePSF.super.getJacobianFunction(xgrid, ygrid).value(point)));
//          IJ.log("analytic jacobian: " + Arrays.deepToString(retVal));
        return retVal;
      }
    };
  }

  @Override
  public double[] getInitialSimplex() {
    return new double[]{1, 1, 3000, 0.1, 0.1, 10, 0.1};
  }

  @Override
  public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
    double[] retValue = new double[7];
    retValue[0] = subImage.detectorX;
    retValue[1] = subImage.detectorY;
    retValue[2] = (subImage.getMax() - subImage.getMin()) * 2 * PI * defaultSigma * defaultSigma;
    retValue[3] = defaultSigma;
    retValue[4] = defaultSigma;
    retValue[5] = subImage.getMin();
    retValue[6] = defaultFi;
    return retValue;
  }

  @Override
  public PSFInstance newInstanceFromParams(double[] params) {
    params[6] = Math.toDegrees(params[6]) % 90d;
    params[6] = params[6] < 0 ? params[6] + 90 : params[6];
    return super.newInstanceFromParams(params);
  }
}
