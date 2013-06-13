package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.*;
import ij.IJ;

/**
 *
 */
public class IterativeQuadraticFitting {

  public static double shiftToOrigin(double[] quadratic1, double[] quadratic2) {
    double intersection;
    if (Math.abs(quadratic1[1] - quadratic2[1]) < 1.0E-6) {
      intersection = (quadratic1[2] - quadratic2[2] + quadratic1[1] * sqr(quadratic1[0]) - quadratic1[1] * sqr(quadratic2[0])) / (2 * quadratic1[1] * (quadratic1[0] - quadratic2[0]));
    } else {
      double root = sqrt(quadratic1[1] * (quadratic2[1] * sqr(quadratic1[0] - quadratic2[0]) - quadratic1[2] + quadratic2[2]) + quadratic2[1] * (quadratic1[2] - quadratic2[2]));
      double intersection1 = -(-root - quadratic1[1] * quadratic1[0] + quadratic2[1] * quadratic2[0]) / (quadratic1[1] - quadratic2[1]);
      double intersection2 = -(root - quadratic1[1] * quadratic1[0] + quadratic2[1] * quadratic2[0]) / (quadratic1[1] - quadratic2[1]);
      intersection = (Math.abs(intersection1 - quadratic1[0]) < Math.abs(intersection2 - quadratic1[0])) ? intersection1 : intersection2;
    }

    IJ.log("intersection: " + intersection);
    quadratic1[0] -= intersection;
    quadratic2[0] -= intersection;
    return intersection;
  }
  private double inlierFraction = 0.90;
  private int maxIterations = 5;

  private double[] fit(double[] x, double[] y, ParametricUnivariateFunction function, double[] initialParams) {
    int numberOfInliers = (int) (inlierFraction * x.length);
    CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer());
    WeightedObservedPoint[] points = new WeightedObservedPoint[x.length];
    //fit using all points
    for (int i = 0; i < x.length; i++) {
      points[i] = new WeightedObservedPoint(1, x[i], y[i]);
      fitter.addObservedPoint(points[i]);
    }
    double[] parameters = fitter.fit(200000, function, initialParams);
    double[] residuals = new double[x.length];
    for (int it = 0; it < maxIterations; it++) {
      //fit again with only inlier points (points with smallest error)
      computeResiduals(parameters, function, x, y, residuals);
      int[] inliers = findIndicesOfSmallestN(residuals, numberOfInliers);

//      System.out.println("residuals : " + Arrays.toString(residuals));
//      System.out.println("inliers : " + Arrays.toString(inliers));

      fitter.clearObservations();
      for (int i : inliers) {
        fitter.addObservedPoint(points[i]);
      }
      parameters = fitter.fit(function, parameters);
    }
    return parameters;
  }

  public double[] fitParams(double[] x, double[] y) {
    double min = y[0];
    int minIndex = 0;
    for (int i = 0; i < x.length; i++) {
      if (y[i] < min) {
        min = y[i];
        minIndex = i;
      }
    }
    return fit(x, y, new QuadraticFunction(), new double[]{x[minIndex], 0.0001, min});
  }

  public double[] fitShift(double[] x, double[] y, double[] fixedParams) {
    return fit(x, y, new ShiftedQuadraticFunctionWFixedParams(fixedParams), new double[]{0});
  }

  private void computeResiduals(double[] parameters, ParametricUnivariateFunction function, double[] x, double[] y, double[] residualArray) {
    for (int i = 0; i < x.length; i++) {
      double dx = y[i] - function.value(x[i], parameters);
      residualArray[i] = dx * dx;
    }
  }

  protected static int[] findIndicesOfSmallestN(final double[] values, int n) {
    int[] result = new int[n];
    PriorityQueue<Integer> topN = new PriorityQueue<Integer>(n, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return -Double.compare(values[o1], values[o2]);
      }
    });
    for (int i = 0; i < values.length; i++) {
      if (topN.size() < n) {
        topN.add(i);
      } else {
        if (values[topN.peek()] > values[i]) {
          topN.remove();
          topN.add(i);
        }
      }
    }
    for (int i = 0; i < result.length; i++) {
      result[i] = topN.remove();
    }
    return result;
  }
}

class QuadraticFunction implements ParametricUnivariateFunction {

  @Override
  public double value(double x, double... parameters) {
    double xsubx0 = x - parameters[0];
    return xsubx0 * xsubx0 * parameters[1] + parameters[2];
  }

  @Override
  public double[] gradient(double x, double... parameters) {
    double xsubx0 = x - parameters[0];
    double[] gradients = new double[3];
    gradients[0] = -2*parameters[1] * xsubx0;
    gradients[1] = xsubx0 * xsubx0;
    gradients[2] = 1;
    return gradients;
  }
}

class ShiftedQuadraticFunctionWFixedParams implements ParametricUnivariateFunction {

  double[] fixedParams;

  public ShiftedQuadraticFunctionWFixedParams(double[] fixedParams) {
    this.fixedParams = fixedParams;
  }

  @Override
  public double value(double x, double... parameters) {
    double xsubx0 = x - parameters[0];
    return xsubx0 * xsubx0 * fixedParams[1] + xsubx0 * fixedParams[2] + fixedParams[3];
  }

  @Override
  public double[] gradient(double x, double... parameters) {
    double[] gradients = new double[1];
    gradients[0] = fixedParams[1] * (-2 * x + 2 * parameters[0]) - fixedParams[2];
    return gradients;
  }
}
