package fi.utu.jaolho.LMA;

import fi.utu.jaolho.LMA.ArrayConverter.SeparatedData;

import java.util.Arrays;

/**
 * A class which implements the <i>Levenberg-Marquardt Algorithm</i>
 * (LMA) fit for non-linear, multidimensional parameter space for any
 * multidimensional fit function.
 * <p>
 *
 * The algorithm is described in <i>Numerical Recipes in FORTRAN</i>, 2nd
 * edition, p. 676-679, ISBN 0-521-43064X, 1992 and also
 * <a href="http://www.nrbook.com/b/bookfpdf/f15-5.pdf">here</a> as a pdf file.
 * <p>
 *
 * The matrix (
 * <code>LMAMatrix</code>) class used in the fit is an interface, so you can use
 * your favourite implementation. This package uses
 * <code>Matrix</code> from JAMA-math libraries, but feel free to use anything
 * you want. Note that you have to implement the actual model function and its
 * partial derivates as
 * <code>LMAFunction</code> or
 * <code>LMAMultiDimFunction</code> before making the fit.
 * <p>
 *
 * Note that there are <i>three</i> different ways to input the data points.
 * Read the documentation for each constructor carefully.
 *
 * @author Janne Holopainen (jaolho@utu.fi, tojotamies@gmail.com)
 * @version 1.2, 24.04.2007
 *
 * The algorithm is free for non-commercial use.
 *
 */
public class LMA {

    /**
     * Set true to print details while fitting.
     */
    public boolean verbose = false;
    public boolean log_errors = false;
    /**
     * The model function to be fitted, y = y(x[], a[]), where
     * <code>x[]</code> the array of x-values and
     * <code>a</code> is the array of fit parameters.
     */
    public LMAMultiDimFunction function;
    /**
     * The array of fit parameters (a.k.a, the a-vector).
     */
    public double[] parameters;
    /**
     * Measured y-data points for which the model function is to be fitted,
     * yDataPoints[j] = y(xDataPoints[j], a[]).
     */
    public double yDataPoints[];
    /**
     * Measured x-data point arrays for which the model function is to be
     * fitted, yDataPoints[j] = y(xDataPoints[j], a[]). xDataPoints.length must
     * be equal to yDataPoints.length and xDataPoints[].length must equal to the
     * fit function's dimension.
     */
    public double xDataPoints[][];
    /**
     * Weights for each data point. The merit function is: chi2 = Sum[(y_i -
     * y(x_i;a))^2 * w_i]. For gaussian errors in datapoints, set w_i =
     * (sigma_i)^-2.
     */
    public double[] weights;
    public LMAMatrix alpha;
    public double[] beta;
    public double[] da;
    public double lambda = 0.001;
    public double lambdaFactor = 10;
    public double incrementedChi2;
    public double[] incrementedParameters;
    public int iterationCount;
    public double chi2;
    // default end conditions
    public double minDeltaChi2 = 1e-30;
    public int maxIterations = 100;

    /**
     * One dimensional convenience constructor for LMAFunction. You can also
     * implement the same function using LMAMultiDimFunction.
     * <p>
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of data points, M is the number of fit parameters. Call
     * <code>fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Must be able to take M
     * input parameters.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in an
     * array, <code>double[0 = x, 1 = y][point index]</code>. Size must
     * be <code>double[2][N]</code>.
     */
    public LMA(final LMAFunction function, double[] parameters, double[][] dataPoints) {
        this(function, parameters, dataPoints, function.constructWeights(dataPoints));
    }

    /**
     * One dimensional convenience constructor for LMAFunction. You can also
     * implement the same function using LMAMultiDimFunction.
     * <p>
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of data points, M is the number of fit parameters. Call
     * <code>fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Must be able to take M
     * input parameters.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in an
     * array, <code>double[0 = x, 1 = y][point index]</code>. Size must
     * be <code>double[2][N]</code>.
     */
    public LMA(final LMAFunction function, double[] parameters, double[][] dataPoints, double[] weights) {
        this(
                // convert LMAFunction to LMAMultiDimFunction
                new LMAMultiDimFunction() {
            private LMAFunction f = function;

            @Override
            public double getPartialDerivate(double[] x, double[] a, int parameterIndex) {
                return f.getPartialDerivate(x[0], a, parameterIndex);
            }

            @Override
            public double getY(double[] x, double[] a) {
                return f.getY(x[0], a);
            }
        },
                parameters,
                dataPoints[1], // y-data
                ArrayConverter.transpose(dataPoints[0]), // x-data
                weights,
                new LMAMatrix(parameters.length, parameters.length));
    }

    /**
     * One dimensional convenience constructor for LMAFunction. You can also
     * implement the same function using LMAMultiDimFunction.
     * <p>
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of data points, M is the number of fit parameters. Call
     * <code>fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Must be able to take M
     * input parameters.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in an
     * array, <code>float[0 = x, 1 = y][point index]</code>. Size must
     * be <code>float[2][N]</code>.
     */
    public LMA(final LMAFunction function, float[] parameters, float[][] dataPoints) {
        this(
                function,
                ArrayConverter.asDoubleArray(parameters),
                ArrayConverter.asDoubleArray(dataPoints));
    }

    /**
     * One dimensional convenience constructor for LMAFunction. You can also
     * implement the same function using LMAMultiDimFunction.
     * <p>
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of data points, M is the number of fit parameters. Call
     * <code>fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Must be able to take M
     * input parameters.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in an
     * array, <code>float[0 = x, 1 = y][point index]</code>. Size must
     * be <code>float[2][N]</code>.
     * @param weights The weights, normally given
     * as: <code>weights[i] = 1 / sigma_i^2</code>. If you have a bad data
     * point, set its weight to zero. If the given array is null, a new array is
     * created with all elements set to 1.
     */
    public LMA(final LMAFunction function, float[] parameters, float[][] dataPoints, float[] weights) {
        this(
                function,
                ArrayConverter.asDoubleArray(parameters),
                ArrayConverter.asDoubleArray(dataPoints),
                ArrayConverter.asDoubleArray(weights));
    }

    /**
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of y-data points, K is the dimension of the fit function
     * and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in two dimensional array where each
     * array, dataPoints[i], contains one y-value followed by the corresponding
     * x-array values. I.e., the arrays should look like this:
     * <p>
     * dataPoints[0] = y0 x00 x01 x02 ... x0[K-1]<br>
     * dataPoints[1] = y1 x10 x11 x12 ... x1[K-1]<br>
     * .
     * ..<br>
     * dataPoints[N] = yN xN0 xN1 xN2 ... x[N-1][K-1]
     */
    public LMA(LMAMultiDimFunction function, float[] parameters, float[][] dataPoints) {
        this(
                function,
                ArrayConverter.asDoubleArray(parameters),
                ArrayConverter.asDoubleArray(dataPoints),
                function.constructWeights(ArrayConverter.asDoubleArray(dataPoints)),
                new LMAMatrix(parameters.length, parameters.length));
    }

    /**
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of y-data points, K is the dimension of the fit function
     * and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in two dimensional array where each
     * array, dataPoints[i], contains one y-value followed by the corresponding
     * x-array values. I.e., the arrays should look like this:
     * <p>
     * dataPoints[0] = y0 x00 x01 x02 ... x0[K-1]<br>
     * dataPoints[1] = y1 x10 x11 x12 ... x1[K-1]<br>
     * .
     * ..<br>
     * dataPoints[N] = yN xN0 xN1 xN2 ... x[N-1][K-1]
     */
    public LMA(LMAMultiDimFunction function, double[] parameters, double[][] dataPoints) {
        this(
                function,
                parameters,
                dataPoints,
                function.constructWeights(dataPoints),
                new LMAMatrix(parameters.length, parameters.length));
    }

    /**
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of y-data points, K is the dimension of the fit function
     * and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param yDataPoints The y-data points in an array.
     * @param xDataPoints The x-data points for each y data point,
     * double[y-index][x-index]
     */
    public LMA(LMAMultiDimFunction function, double[] parameters, float[] yDataPoints, float[][] xDataPoints) {
        this(
                function,
                parameters,
                ArrayConverter.asDoubleArray(yDataPoints),
                ArrayConverter.asDoubleArray(xDataPoints),
                function.constructWeights(ArrayConverter.combineMultiDimDataPoints(yDataPoints, xDataPoints)),
                new LMAMatrix(parameters.length, parameters.length));
    }

    /**
     * Initiates the fit with function constructed weights and a JAMA matrix. N
     * is the number of y-data points, K is the dimension of the fit function
     * and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param yDataPoints The y-data points in an array.
     * @param xDataPoints The x-data points for each y data point,
     * double[y-index][x-index]
     */
    public LMA(LMAMultiDimFunction function, double[] parameters, double[] yDataPoints, double[][] xDataPoints) {
        this(
                function,
                parameters,
                yDataPoints,
                xDataPoints,
                function.constructWeights(ArrayConverter.combineMultiDimDataPoints(yDataPoints, xDataPoints)),
                new LMAMatrix(parameters.length, parameters.length));
    }

    /**
     * Initiates the fit. N is the number of y-data points, K is the dimension
     * of the fit function and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in two dimensional array where each
     * array, dataPoints[i], contains one y-value followed by the corresponding
     * x-array values. I.e., the arrays should look like this:
     * <p>
     * dataPoints[0] = y0 x00 x01 x02 ... x0[K-1]<br>
     * dataPoints[1] = y1 x10 x11 x12 ... x1[K-1]<br>
     * .
     * ..<br>
     * dataPoints[N] = yN xN0 xN1 xN2 ... x[N-1][K-1]
     * <p>
     * @param weights The weights, normally given
     * as: <code>weights[i] = 1 / sigma_i^2</code>. If you have a bad data
     * point, set its weight to zero. If the given array is null, a new array is
     * created with all elements set to 1.
     * @param alpha An LMAMatrix instance. Must be initiated to (M x M) size.
     */
    public LMA(LMAMultiDimFunction function, float[] parameters, float[][] dataPoints, float[] weights, LMAMatrix alpha) {
        this(
                function,
                ArrayConverter.asDoubleArray(parameters),
                ArrayConverter.asDoubleArray(dataPoints),
                ArrayConverter.asDoubleArray(weights),
                alpha);
    }

    /**
     * Initiates the fit. N is the number of y-data points, K is the dimension
     * of the fit function and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Input parameter sizes K
     * and M.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param dataPoints The data points in two dimensional array where each
     * array, dataPoints[i], contains one y-value followed by the corresponding
     * x-array values. I.e., the arrays should look like this:
     * <p>
     * dataPoints[0] = y0 x00 x01 x02 ... x0[K-1]<br>
     * dataPoints[1] = y1 x10 x11 x12 ... x1[K-1]<br>
     * .
     * ..<br>
     * dataPoints[N] = yN xN0 xN1 xN2 ... x[N-1][K-1]
     * <p>
     * @param weights The weights, normally given
     * as: <code>weights[i] = 1 / sigma_i^2</code>. If you have a bad data
     * point, set its weight to zero. If the given array is null, a new array is
     * created with all elements set to 1.
     * @param alpha An LMAMatrix instance. Must be initiated to (M x M) size.
     */
    public LMA(LMAMultiDimFunction function, double[] parameters, double[][] dataPoints, double[] weights, LMAMatrix alpha) {
        SeparatedData s = ArrayConverter.separateMultiDimDataToXY(dataPoints);
        this.yDataPoints = s.yDataPoints;
        this.xDataPoints = s.xDataPoints;
        init(function, parameters, yDataPoints, xDataPoints, weights, alpha);
    }

    /**
     * Initiates the fit. N is the number of y-data points, K is the dimension
     * of the fit function and M is the number of fit parameters. Call
     * <code>this.fit()</code> to start the actual fitting.
     *
     * @param function The model function to be fitted. Must be able to take M
     * input parameters.
     * @param parameters The initial guess for the fit parameters, length M.
     * @param yDataPoints The y-data points in an array.
     * @param xDataPoints The x-data points for each y data point,
     * double[y-index][x-index] Size must be <code>double[N][K]</code>, where N
     * is the number of measurements and K is the dimension of the fit function.
     * @param weights The weights, normally given
     * as: <code>weights[i] = 1 / sigma_i^2</code>. If you have a bad data
     * point, set its weight to zero. If the given array is null, a new array is
     * created with all elements set to 1.
     * @param alpha An LMAMatrix instance. Must be initiated to (M x M) size.
     */
    public LMA(LMAMultiDimFunction function, double[] parameters, double[] yDataPoints, double[][] xDataPoints, double[] weights, LMAMatrix alpha) {
        init(function, parameters, yDataPoints, xDataPoints, weights, alpha);
    }

    protected void init(LMAMultiDimFunction function, double[] parameters, double[] yDataPoints, double[][] xDataPoints, double[] weights, LMAMatrix alpha) {
        if (yDataPoints.length != xDataPoints.length) {
            throw new IllegalArgumentException("Data must contain an x-array for each y-value. Check your xDataPoints-array.");
        }
        this.function = function;
        this.parameters = parameters;
        this.yDataPoints = yDataPoints;
        this.xDataPoints = xDataPoints;
        this.weights = checkWeights(yDataPoints.length, weights);
        this.incrementedParameters = new double[parameters.length];
        this.alpha = alpha;
        this.beta = new double[parameters.length];
        this.da = new double[parameters.length];
    }

    /**
     * The default fit. If used after calling fit(lambda, minDeltaChi2,
     * maxIterations), uses those values. The stop condition is fetched from
     * <code>this.stop()</code>. Override
     * <code>this.stop()</code> if you want to use another stop condition.
     */
    public void fit() throws LMAMatrix.InvertException {
        iterationCount = 0;
        if (Double.isNaN(calculateChi2())) {
            throw new RuntimeException("INITIAL PARAMETERS ARE ILLEGAL.");
        }
        do {
            chi2 = calculateChi2();
            if (verbose) {
                System.out.println(iterationCount + ": chi2 = " + chi2 + ", " + Arrays.toString(parameters));
            }
            updateAlpha();
            updateBeta();
            try {
                solveIncrements();
                incrementedChi2 = calculateIncrementedChi2();
                // The guess results to worse chi2 or NaN - make the step smaller
                if (incrementedChi2 >= chi2 || Double.isNaN(incrementedChi2)) {
                    lambda *= lambdaFactor;
                } // The guess results to better chi2 - move and make the step larger
                else {
                    lambda /= lambdaFactor;
                    updateParameters();
                }
            } catch (LMAMatrix.InvertException e) {
                // If the error happens on the last round, the fit has failed - throw the error out
                if (iterationCount == maxIterations) {
                    throw e;
                }
                // otherwise make the step smaller and try again
                if (verbose) {
                    System.out.println(e.getMessage());
                }
                lambda *= lambdaFactor;
            }
            iterationCount++;
        } while (!stop());
        printEndReport();
    }

    private void printEndReport() {
        if (verbose) {
            System.out.println(" ***** FIT ENDED ***** ");
            System.out.println(" Goodness: " + chi2Goodness());
            try {
                System.out.println(" Parameter std errors: " + Arrays.toString(getStandardErrorsOfParameters()));
            } catch (LMAMatrix.InvertException e) {
                System.err.println(" Fit ended OK, but cannot calculate covariance matrix.");
                System.out.println(" ********************* ");
            }
            System.out.println(" ********************* ");
        }
    }

    /**
     * Initializes and starts the fit. The stop condition is fetched from
     * <code>this.stop()</code>. Override
     * <code>this.stop()</code> if you want to use another stop condition.
     */
    public void fit(double lambda, double minDeltaChi2, int maxIterations) throws LMAMatrix.InvertException {
        this.lambda = lambda;
        this.minDeltaChi2 = minDeltaChi2;
        this.maxIterations = maxIterations;
        fit();
    }

    /**
     * The stop condition for the fit. Override this if you want to use another
     * stop condition.
     */
    public boolean stop() {
        return Math.abs(chi2 - incrementedChi2) < minDeltaChi2 || iterationCount > maxIterations;
    }

    /**
     * Updates parameters from incrementedParameters.
     */
    protected void updateParameters() {
        System.arraycopy(incrementedParameters, 0, parameters, 0, parameters.length);
    }

    /**
     * Solves the increments array (
     * <code>this.da</code>) using alpha and beta. Then updates the
     * <code>this.incrementedParameters</code> array. NOTE: Inverts alpha. Call
     * at least
     * <code>updateAlpha()</code> before calling this.
     */
    protected void solveIncrements() throws LMAMatrix.InvertException {
        alpha.invert(); // throws InvertException if matrix is singular
        alpha.multiply(beta, da);
        for (int i = 0; i < parameters.length; i++) {
            incrementedParameters[i] = parameters[i] + da[i];
        }
    }

    /**
     * @return The calculated evalution function value (chi2) for the given
     * parameter array. NOTE: Does not change the value of chi2.
     */
    protected double calculateChi2(double[] a) {
        double result = 0;
        for (int i = 0; i < yDataPoints.length; i++) {
            double dy = yDataPoints[i] - function.getY(xDataPoints[i], a);
            // check if NaN occurred
            if (Double.isNaN(dy)) {
                if(log_errors) {
                    System.err.println(
                            "Chi2 calculation produced a NaN value at point " + i + ":\n"
                            + " x = " + Arrays.toString(xDataPoints[i]) + "\n"
                            + " y = " + yDataPoints[i] + "\n"
                            + " parameters: " + Arrays.toString(a) + "\n"
                            + " iteration count = " + iterationCount);
                }
                return Double.NaN;
            }
            result += weights[i] * dy * dy;
        }
        
        return result;
    }

    /**
     * @return The calculated evaluation function value for the current fit
     * parameters. NOTE: Does not change the value of chi2.
     */
    protected double calculateChi2() {
        return calculateChi2(parameters);
    }

    /**
     * @return The calculated evaluation function value for the incremented
     * parameters (da + a). NOTE: Does not change the value of chi2.
     */
    protected double calculateIncrementedChi2() {
        return calculateChi2(incrementedParameters);
    }

    /**
     * Calculates all elements for
     * <code>this.alpha</code>.
     */
    protected void updateAlpha() {
        for (int i = 0; i < parameters.length; i++) {
            for (int j = 0; j < parameters.length; j++) {
                alpha.setElement(i, j, calculateAlphaElement(i, j));
            }
        }
    }

    /**
     * @return An calculated lambda weighted element for the alpha-matrix. NOTE:
     * Does not change the value of alpha-matrix.
     */
    protected double calculateAlphaElement(int row, int col) {
        double result = 0;
        for (int i = 0; i < yDataPoints.length; i++) {
            result +=
                    weights[i]
                    * function.getPartialDerivate(xDataPoints[i], parameters, row)
                    * function.getPartialDerivate(xDataPoints[i], parameters, col);
        }
        // Marquardt's lambda addition
        if (row == col) {
            result *= (1 + lambda);
        }
        return result;
    }

    /**
     * Calculates all elements for
     * <code>this.beta</code>.
     */
    protected void updateBeta() {
        for (int i = 0; i < parameters.length; i++) {
            beta[i] = calculateBetaElement(i);
        }
    }

    /**
     * @return An calculated element for the beta-matrix. NOTE: Does not change
     * the value of beta-matrix.
     */
    protected double calculateBetaElement(int row) {
        double result = 0;
        for (int i = 0; i < yDataPoints.length; i++) {
            result +=
                    weights[i]
                    * (yDataPoints[i] - function.getY(xDataPoints[i], parameters))
                    * function.getPartialDerivate(xDataPoints[i], parameters, row);
        }
        return result;
    }

    /**
     * @return Estimate for goodness of fit, used for binned data, Sum[(y_data -
     * y_fit)^2 / y_data]
     */
    public float getRelativeChi2() {
        float result = 0;
        for (int i = 0; i < yDataPoints.length; i++) {
            double dy = yDataPoints[i] - function.getY(xDataPoints[i], parameters);
            if (yDataPoints[i] != 0) {
                result += (float) (dy * dy) / yDataPoints[i];
            }
        }
        return result;
    }

    /**
     * @return Estimate for goodness of fit, Sum[|y_data - y_fit| / y_fit] / n
     */
    public float getMeanRelativeError() {
        float result = 0;
        for (int i = 0; i < yDataPoints.length; i++) {
            double fy = function.getY(xDataPoints[i], parameters);
            double dy = Math.abs(yDataPoints[i] - fy);
            if (fy != 0) {
                result += (float) (dy / fy);
            }
        }
        return result / (float) yDataPoints.length;
    }

    /**
     * @return Estimate for goodness of fit, Sum[(y_data - y_fit)^2] / n
     */
    public float chi2Goodness() {
        return (float) (chi2 / (double) (yDataPoints.length - parameters.length));
    }

    /**
     * Checks that the given array in not null, filled with zeros or contain
     * negative weights.
     *
     * @return A valid weights array.
     */
    protected double[] checkWeights(int length, double[] weights) {
        boolean damaged = false;
        // check for null
        if (weights == null) {
            damaged = true;
            weights = new double[length];
        } // check if all elements are zeros or if there are negative, NaN or Infinite elements
        else {
            boolean allZero = true;
            boolean illegalElement = false;
            for (int i = 0; i < weights.length && !illegalElement; i++) {
                if (weights[i] < 0 || Double.isNaN(weights[i]) || Double.isInfinite(weights[i])) {
                    illegalElement = true;
                }
                allZero = (weights[i] == 0) && allZero;
            }
            damaged = allZero || illegalElement;
        }
        if (!damaged) {
            return weights;
        }
        System.out.println("WARNING: weights were not well defined. All elements set to 1.");
        Arrays.fill(weights, 1);
        return weights;
    }

    /**
     * @return The covariance matrix of the fit parameters.
     * @throws LMAMatrix.InvertException if the inversion of alpha fails. Note
     * that even if the fit does NOT throw the invert exception, this method can
     * still do it, because here alpha is inverted with lambda = 0.
     */
    public double[][] getCovarianceMatrixOfStandardErrorsInParameters() throws LMAMatrix.InvertException {
        double[][] result = new double[parameters.length][parameters.length];
        double oldLambda = lambda;
        lambda = 0;
        updateAlpha();
        try {
            alpha.invert();
        } catch (LMAMatrix.InvertException e) {
            // restore alpha just in case
            lambda = oldLambda;
            updateAlpha();
            throw new LMAMatrix.InvertException("Inverting alpha failed with lambda = 0\n" + e.getMessage());
        }
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                result[i][j] = alpha.getElement(i, j);
            }
        }
        alpha.invert();
        lambda = oldLambda;
        updateAlpha();
        return result;
    }

    /**
     * @return The estimated standard errors of the fit parameters.
     * @throws LMAMatrix.InvertException if the inversion of alpha fails. Note
     * that even if the fit does NOT throw the invert exception, this method can
     * still do it, because here alpha is inverted with lambda = 0.
     */
    public double[] getStandardErrorsOfParameters() throws LMAMatrix.InvertException {
        double[][] cov = getCovarianceMatrixOfStandardErrorsInParameters();
        if (cov == null) {
            return null;
        }
        double[] result = new double[parameters.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.sqrt(cov[i][i]);
        }
        return result;
    }

    /**
     * @return Fit function values with the current x- and parameter-values.
     */
    public double[] generateData() {
        return function.generateData(this);
    }
}
