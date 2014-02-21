package cz.cuni.lf1.lge.ThunderSTORM.util;

import org.apache.commons.math3.util.FastMath;

/**
 * This class wraps mathematical functions used in ThunderSTORM.
 *
 * The reason for this is that the common java.lang.Math class is missing some
 * of the methods we need to use. Also the methods in the class are a bit slow
 * for our needs. So it is more convenient to have this class (Adapter pattern)
 * to not need to commit changes inside the application code and instead of that
 * use methods in this class to globaly change the methods we want to use. Our
 * decision was to use highly optimized FastMath class from Apache Commons Math3
 * library, which performs much better in our scenario (many calls to
 * {@code exp}) than the general Java Math class.
 */
public class MathProxy {

    /**
     * PI constant ({@mathjax \pi}).
     */
    public static final double PI = org.apache.commons.math3.util.FastMath.PI;

    /**
     * Raise a double to an int power.
     *
     * @param x number to raise
     * @param n exponent
     * @return {@mathjax x^n}
     */
    public static double pow(double x, int n) {
        return org.apache.commons.math3.util.FastMath.pow(x, n);
    }

    /**
     * Raise a double to a double power.
     *
     * @param x number to raise
     * @param n exponent
     * @return {@mathjax x^n}
     */
    public static double pow(double x, double n) {
        return org.apache.commons.math3.util.FastMath.pow(x, n);
    }

    /**
     * Exponential function.
     *
     * @param x a value
     * @return {@mathjax \mathrm{e}^x}
     */
    public static double exp(double x) {
        return org.apache.commons.math3.util.FastMath.exp(x);
    }
    
    /**
     * Natural logarithm.
     *
     * @param x a value
     * @return {@mathjax \mathrm{log}(x)}
     */
    public static double log(double x) {
        return org.apache.commons.math3.util.FastMath.log(x);
    }

    /**
     * Get the smallest whole number larger than x.
     *
     * @param x number from which ceil is requested
     * @return {@mathjax \lceil x \rceil}
     */
    public static double ceil(double x) {
        return org.apache.commons.math3.util.FastMath.ceil(x);
    }

    /**
     * Compute the maximum of two values.
     *
     * @param a first value
     * @param b second value
     * @return b if a is lesser or equal to b, a otherwise
     */
    public static int max(int a, int b) {
        return org.apache.commons.math3.util.FastMath.max(a, b);
    }

    /**
     * Get the closest long to x.
     *
     * @param x number from which closest long is requested
     * @return closest long to x
     */
    public static long round(double x) {
        return org.apache.commons.math3.util.FastMath.round(x);
    }

    /**
     * Get the closest int to x.
     *
     * @param x number from which closest int is requested
     * @return closest int to x
     */
    public static int round(float x) {
        return org.apache.commons.math3.util.FastMath.round(x);
    }

    /**
     * Compute the square root of a number.
     *
     * @param x number on which evaluation is done
     * @return {@mathjax \sqrt{a}}
     */
    public static double sqrt(double x) {
        return org.apache.commons.math3.util.FastMath.sqrt(x);
    }

    /**
     * Compute the square of a number.
     *
     * @param x a number
     * @return {@mathjax x^2}
     */
    public static double sqr(double x) {
        return x * x;
    }

    /**
     * Evaluates the 1D Gaussian function at a given point {@code x} and with a
     * given width {@code sigma}.
     *
     * @param x a point where the function will get evaluated
     * @param sigma {@mathjax \sigma} is a width of the Gaussian function
     * @param normalized decides wheter the Gaussian should be normalized to
     * have its integral equal to 1
     * @return if {@code normalized} is {@code true} then the function returns
     * {@mathjax \frac{1}{\sqrt{2\pi\sigma^2}} e^{-\frac{x^2}{2 \sigma^2}}}, else
     * the function returns {@mathjax e^{-\frac{x^2}{2 \sigma^2}}}.
     */
    public static double gauss(double x, double sigma, boolean normalized) {
        return exp(-0.5 * sqr(x / sigma)) / ((normalized) ? (sigma * sqrt(2 * PI)) : 1);
    }

    public static Double[] add(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(arr[i].doubleValue() + v);
        }
        return res;
    }

    public static Double abs(Double val) {
        return new Double(java.lang.Math.abs(val.doubleValue()));
    }

    public static double toRadians(Double get) {
        return FastMath.toRadians(PI);
    }

    public static double toDegrees(double x) {
        return FastMath.toDegrees(x);
    }

    public static double sin(double radians) {
        return FastMath.sin(radians);
    }

    public static double cos(double radians) {
        return FastMath.cos(radians);
    }

    public static double atan2(double y, double x) {
        return FastMath.atan2(y, x);
    }
    
    public static double atan(double x) {
        return FastMath.atan(x);
    }
    
    public static double tan(double x) {
        return FastMath.tan(x);
    }

    public static int min(int a, int b) {
        return FastMath.min(a, b);
    }

    public static double min(double a, double b) {
        return FastMath.min(a, b);
    }

    public static double max(double a, double b) {
        return FastMath.max(a, b);
    }
    
    public static double max(double... values) {
        double max = values[0];
        for(int i = 1; i < values.length; i++) {
            if(values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }
    
    public static int [] genIntSequence(int from, int length) {
        int [] seq = new int[length];
        for(int i = 0; i < length; i++) {
            seq[i] = from + i;
        }
        return seq;
    }
}