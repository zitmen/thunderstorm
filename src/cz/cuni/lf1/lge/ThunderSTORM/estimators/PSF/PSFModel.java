package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import static org.apache.commons.math3.util.FastMath.log;

/**
 * Representation of PSFModel model.
 *
 * <strong>Note:</strong> in a future release the PSFModel will be more abstract
 * to allow easily work with any possible PSFModel out there, but right we use
 * strictly the symmetric 2D Gaussian model.
 *
 * <strong>This class and its children need to be refactored!</strong>
 */
public abstract class PSFModel {

  /**
   * Returns names of parameters returned by {@code getParams} method, thus the
   * order of returned elements must correspond to each other.
   *
   * @return parameters' titles as an array
   */
  public abstract String[] getParamNames();

  public double[] transformParameters(double[] params) {
    return params;
  }

  public double[] transformParametersInverse(double[] params) {
    return params;
  }

  /**
   * expected value for mle calculation
   *
   * @param params
   * @param x
   * @param y
   * @return
   */
  abstract public double getExpectedValue(double[] params, int x, int y);

  abstract public double getValue(double[] params, double x, double y);

  public MultivariateVectorFunction getValueFunction(final int[] xgrid, final int[] ygrid) {
    return new MultivariateVectorFunction() {
      @Override
      public double[] value(double[] params) throws IllegalArgumentException {
        double[] transformedParams = transformParameters(params);
        double[] retVal = new double[xgrid.length];
        for (int i = 0; i < xgrid.length; i++) {
          retVal[i] = getValue(transformedParams, xgrid[i], ygrid[i]);
        }
        return retVal;
      }
    };
  }

  /**
   * Default implementation with numeric gradients. You can override it with
   * analytical jacobian.
   */
  public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
    final MultivariateVectorFunction valueFunction = getValueFunction(xgrid, ygrid);
    return new MultivariateMatrixFunction() {
      static final double step = 0.1;

      @Override
      public double[][] value(double[] point) throws IllegalArgumentException {
        double[][] retVal = new double[xgrid.length][point.length];

        for (int i = 0; i < point.length; i++) {
          double[] newPoint = point.clone();
          newPoint[i] = newPoint[i] + step;
          double[] f1 = valueFunction.value(newPoint);
          double[] f2 = valueFunction.value(point);
          for (int j = 0; j < f1.length; j++) {
            retVal[j][i] = (f1[j] - f2[j]) / step;
          }
        }
        return retVal;
      }
    };
  }

  public MultivariateFunction getLikelihoodFunction(final int[] xgrid, final int[] ygrid, final double[] imageValues) {
    return new MultivariateFunction() {
      @Override
      public double value(double[] point) {
        double[] newPoint = transformParameters(point);


        double logLikelihood = 0;
        for (int i = 0; i < xgrid.length; i++) {
          double expectedValue = getValue(newPoint, xgrid[i], ygrid[i]);
          double log = log(expectedValue);
          if (log < -1e6) {
            log = -1e6;
          }
          logLikelihood += expectedValue - imageValues[i] * log;
        }
//        IJ.log("likelihood:" + logLikelihood);
//        IJ.log("point: " + Arrays.toString(point));
        return logLikelihood;
      }
    };
  }

  public abstract double[] getInitialParams(OneLocationFitter.SubImage subImage);

  public PSFInstance newInstanceFromParams(double[] params) {
    return new PSFInstance(getParamNames(), params);
  }
}
