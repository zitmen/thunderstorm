package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Comparator;
import java.util.PriorityQueue;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.SimplePointChecker;

public class IterativeFitting {

    private double inlierFraction = 0.9;
    private int maxIterations = 5;

    public static void shiftInZ(double[] quadratic, double shift) {
        quadratic[0] -= shift;
    }

    private double[] fit(double[] x, double[] y, ParametricUnivariateFunction function, double[] initialParams, int maxIter) {
        int numberOfInliers = (int) (inlierFraction * x.length);
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-10, 10e-10, maxIter)));
        WeightedObservedPoint[] points = new WeightedObservedPoint[x.length];
        //fit using all points
        for(int i = 0; i < x.length; i++) {
            points[i] = new WeightedObservedPoint(1, x[i], y[i]);
            fitter.addObservedPoint(points[i]);
        }
        double[] parameters = fitter.fit(maxIter, function, initialParams);
        double[] residuals = new double[x.length];
        for(int it = 0; it < maxIterations; it++) {
            //fit again with only inlier points (points with smallest error)
            computeResiduals(parameters, function, x, y, residuals);
            int[] inliers = findIndicesOfSmallestN(residuals, numberOfInliers);

//          System.out.println("residuals : " + Arrays.toString(residuals));
//          System.out.println("inliers : " + Arrays.toString(inliers));
            fitter.clearObservations();
            for(int i : inliers) {
                fitter.addObservedPoint(points[i]);
            }
            try {
                parameters = fitter.fit(maxIter, function, parameters);
            } catch(TooManyEvaluationsException ex) {
                if(it > 0) {
                    return parameters;
                } else {
                    throw ex;
                }
            }
        }
        return parameters;
    }

    public DefocusFunction fitParams(DefocusFunction defocusModel, double[] x, double[] y, int maxIter) {
        double min = y[0];
        int minIndex = 0;
        for(int i = 0; i < x.length; i++) {
            if(y[i] < min) {
                min = y[i];
                minIndex = i;
            }
        }
        return defocusModel.getNewInstance(fit(x, y, defocusModel.getFittingFunction(), defocusModel.getInitialParams(x[minIndex], min), maxIter), false);
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
