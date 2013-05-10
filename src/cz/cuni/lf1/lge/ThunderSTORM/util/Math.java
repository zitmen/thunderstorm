package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.Arrays;

/**
 * This class wraps mathematical functions used in ThunderSTORM.
 * 
 * The reason for this is that the common java.lang.Math class is missing some of the
 * methods we need to use. Also the methods in the class are a bit slow for our needs.
 * So it is more convenient to have this class (Adapter pattern) to not need to commit
 * changes inside the application code and instead of that use methods in this class
 * to globaly change the methods we want to use.
 * Our decision was to use highly optimized FastMath class from Apache Commons Math3
 * library, which performs much better in our scenario (many calls to {@code exp})
 * than the general Java Math class.
 */
public class Math {
    
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
     * Compute the sum of an array of floats.
     * 
     * @param arr an array of floats
     * @return {@mathjax \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static float sum(float [] arr) {
        float sum = 0.0f;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    /**
     * Compute the sum of an array of doubles.
     * 
     * @param arr an array of doubles
     * @return {@mathjax \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double sum(double [] arr) {
        double sum = 0.0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    /**
     * Compute the mean value of an array of floats.
     * 
     * @param arr an array of floats
     * @return {@mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static float mean(float [] arr) {
        return sum(arr) / (float) arr.length;
    }
    
    /**
     * Compute the mean value of an array of doubles.
     * 
     * @param arr an array of doubles
     * @return {@mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double mean(double [] arr) {
        return sum(arr) / (double) arr.length;
    }
    
    /**
     * Compute the standard deviation of an array of floats.
     * 
     * @param arr an array of floats
     * @return {@mathjax \sqrt{\frac{1}{arr.length} \sum_{i=0}^{arr.length}{\left(arr[i] - \mu\right)}}},
     *         where {@mathjax \mu} = {@code mean(arr)}.
     */
    public static float stddev(float [] arr) {
        float sumdev = 0.0f, mean = mean(arr);
        for (int i = 0; i < arr.length; i++)
            sumdev += sqr(arr[i] - mean);
        return (float) sqrt(sumdev / (float) arr.length);
    }
    
    /**
     * Evaluates the 1D Gaussian function at a given point {@code x} and with a given width {@code sigma}.
     * 
     * @param x a point where the function will get evaluated
     * @param sigma {@mathjax \sigma} is a width of the Gaussian function
     * @param normalized decides wheter the Gaussian should be normalized to have its integral equal to 1
     * @return if {@code normalized} is {@code true} then the function returns {@mathjax \frac{1}{\sqrt{2\pi\sigma^2}} e^{-\frac{x^2}{2 \sigma^2}}},
     *         else the function returns {@mathjax e^{-\frac{x^2}{2 \sigma^2}}}.
     */
    public static double gauss(double x, double sigma, boolean normalized) {
        return exp(-0.5 * sqr(x / sigma)) / ((normalized) ? (sigma*sqrt(2*PI)) : 1);
    }
    
    /**
     * Compute the median of an array of floats.
     * 
     * @param arr an array of floats
     * @return The median is found by arranging all the observations from lowest value to highest value
     *         and picking the middle one (eg, the median of {3, 5, 9} is 5). If there is an even number
     *         of observations, then there is no single middle value; the median is then defined to be
     *         the mean of the two middle values.
     */
    public static float median(float [] arr) {
        float [] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);
        if((sorted.length % 2 )== 1)    // odd length
            return sorted[sorted.length/2];
        else    // even length
            return (sorted[sorted.length/2] + sorted[1+sorted.length/2]) / 2.0f;
    }
}
