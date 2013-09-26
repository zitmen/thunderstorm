package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.*;

/**
 *
 */
public class IterativeQuadraticFitting {

    private double inlierFraction = 0.9;
    private int maxIterations = 5;

    public static void shiftInZ(double[] quadratic, double shift) {
        quadratic[0] -= shift;
    }

    public static double intersectionOfQuadraticPolynomials(QuadraticFunction quadratic1, QuadraticFunction quadratic2) {
        double intersection;
        double c1 = quadratic1.getC();
        double c2 = quadratic2.getC();
        double a1 = quadratic1.getA();
        double a2 = quadratic2.getA();
        double b1 = quadratic1.getB();
        double b2 = quadratic2.getB();
        if(Math.abs(a1 - a2) < 1.0E-6) {
            intersection = (b1 - b2 + a1 * sqr(c1) - a1 * sqr(c2)) / (2 * a1 * (c1 - c2));
        } else {
            double root = sqrt(a1 * (a2 * sqr(c1 - c2) - b1 + b2) + a2 * (b1 - b2));
            double intersection1 = -(-root - a1 * c1 + a2 * c2) / (a1 - a2);
            double intersection2 = -(root - a1 * c1 + a2 * c2) / (a1 - a2);
            intersection = (Math.abs(intersection1 - c1) < Math.abs(intersection2 - c1)) ? intersection1 : intersection2;
        }
        return intersection;
    }

    private double[] fit(double[] x, double[] y, ParametricUnivariateFunction function, double[] initialParams) {
        int numberOfInliers = (int) (inlierFraction * x.length);
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer());
        WeightedObservedPoint[] points = new WeightedObservedPoint[x.length];
        //fit using all points
        for(int i = 0; i < x.length; i++) {
            points[i] = new WeightedObservedPoint(1, x[i], y[i]);
            fitter.addObservedPoint(points[i]);
        }
        double[] parameters = fitter.fit(200000, function, initialParams);
        double[] residuals = new double[x.length];
        for(int it = 0; it < maxIterations; it++) {
            //fit again with only inlier points (points with smallest error)
            computeResiduals(parameters, function, x, y, residuals);
            int[] inliers = findIndicesOfSmallestN(residuals, numberOfInliers);

//      System.out.println("residuals : " + Arrays.toString(residuals));
//      System.out.println("inliers : " + Arrays.toString(inliers));

            fitter.clearObservations();
            for(int i : inliers) {
                fitter.addObservedPoint(points[i]);
            }
            parameters = fitter.fit(function, parameters);
        }
        return parameters;
    }

    public QuadraticFunction fitParams(double[] x, double[] y) {
        double min = y[0];
        int minIndex = 0;
        for(int i = 0; i < x.length; i++) {
            if(y[i] < min) {
                min = y[i];
                minIndex = i;
            }
        }
        return new QuadraticFunction(fit(x, y, new QuadraticFunction(), new double[]{x[minIndex], 0.0001, min}));
    }

    private void computeResiduals(double[] parameters, ParametricUnivariateFunction function, double[] x, double[] y, double[] residualArray) {
        for(int i = 0; i < x.length; i++) {
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
        for(int i = 0; i < values.length; i++) {
            if(topN.size() < n) {
                topN.add(i);
            } else {
                if(values[topN.peek()] > values[i]) {
                    topN.remove();
                    topN.add(i);
                }
            }
        }
        for(int i = 0; i < result.length; i++) {
            result[i] = topN.remove();
        }
        return result;
    }
}
