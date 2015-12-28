package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.SimplePointChecker;

public class IterativeFitting {

    private double inlierFraction;
    private int maxIterations;

    public IterativeFitting(int maxIterations, double inlierFraction) {
        this.maxIterations = maxIterations;
        this.inlierFraction = inlierFraction;
    }

    private double[] fit(double[] x, double[] y, ParametricUnivariateFunction function, double[] initialParams, int maxIter) {
        int numberOfInliers = (int) (inlierFraction * x.length);
        CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-3, 10e-3, maxIter)));
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
        return defocusModel.getNewInstance(defocusModel.transformParams(fit(x, y, defocusModel.getFittingFunction(), defocusModel.transformParamsInverse(defocusModel.getInitialParams(x[minIndex], min)), maxIter)), false);
    }

    private void computeResiduals(double[] parameters, ParametricUnivariateFunction function, double[] x, double[] y, double[] residualArray) {
        for(int i = 0; i < x.length; i++) {
            double dx = y[i] - function.value(x[i], parameters);
            residualArray[i] = dx * dx;
        }
    }

    protected static int[] findIndicesOfSmallestN(final double[] values, int n) {
        if (values.length < n) {
            throw new IllegalArgumentException("`values` must not have less than `n` elements!");
        }

        Integer[] indices = new Integer[values.length];
        for(int i = 0; i < values.length; i++) indices[i] = i;
        Arrays.sort(indices, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(values[o1], values[o2]);
            }
        });

        int[] result = new int[n];
        for(int i = 0; i < result.length; i++) result[i] = indices[i];
        return result;
    }
}
