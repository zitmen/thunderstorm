package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import java.util.Arrays;
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
    public static float sum(float[] arr) {
        float sum = 0.0f;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public static double[] add(double[] arr, double scalar) {
        if(scalar != 0) {
            for(int i = 0; i < arr.length; i++) {
                arr[i] += scalar;
            }
        }
        return arr;
    }

    /**
     * Compute the sum of an array of doubles.
     *
     * @param arr an array of doubles
     * @return {@mathjax \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double sum(double[] arr) {
        double sum = 0.0;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    /**
     * Compute the mean value of an array of floats.
     *
     * @param arr an array of floats
     * @return {@mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static float mean(float[] arr) {
        return sum(arr) / (float) arr.length;
    }

    /**
     * Compute the mean value of an array of doubles.
     *
     * @param arr an array of doubles
     * @return {@mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double mean(double[] arr) {
        return sum(arr) / (double) arr.length;
    }

    /**
     * Compute the standard deviation of an array of floats.
     *
     * @param arr an array of floats
     * @return {@mathjax \sqrt{\frac{1}{arr.length} \sum_{i=0}^{arr.length}{\left(arr[i]
     * - \mu\right)}}}, where {@mathjax \mu} = {@code mean(arr)}.
     */
    public static float stddev(float[] arr) {
        float sumdev = 0.0f, mean = mean(arr);
        for(int i = 0; i < arr.length; i++) {
            sumdev += sqr(arr[i] - mean);
        }
        return (float) sqrt(sumdev / (float) arr.length);
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

    /**
     * Compute the median of an array of floats.
     *
     * @param arr an array of floats
     * @return The median is found by arranging all the observations from lowest
     * value to highest value and picking the middle one (eg, the median of {3,
     * 5, 9} is 5). If there is an even number of observations, then there is no
     * single middle value; the median is then defined to be the mean of the two
     * middle values.
     */
    public static float median(float[] arr) {
        float[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);
        if((sorted.length % 2) == 1) // odd length
        {
            return sorted[sorted.length / 2];
        } else // even length
        {
            return (sorted[sorted.length / 2] + sorted[1 + sorted.length / 2]) / 2.0f;
        }
    }

    public static double max(double[] array) {
        double max = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static double min(double[] array) {
        double min = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static Number max(Number[] array) {
        Number max = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i].doubleValue() > max.doubleValue()) {
                max = array[i];
            }
        }
        return max;
    }

    public static Number min(Number[] array) {
        Number min = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i].doubleValue() < min.doubleValue()) {
                min = array[i];
            }
        }
        return min;
    }

    public static Double sum(Number[] arr) {
        double sum = 0.0;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i].doubleValue();
        }
        return new Double(sum);
    }

    public static Double mean(Number[] arr) {
        return new Double(sum(arr).doubleValue() / (double) arr.length);
    }

    public static Double stddev(Number[] arr) {
        double sumdev = 0.0, mean = mean(arr);
        for(int i = 0; i < arr.length; i++) {
            sumdev += sqr(arr[i].doubleValue() - mean);
        }
        return new Double(sqrt(sumdev / (double) arr.length));
    }

    public static Number median(Number[] arr) {
        Number[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);
        if((sorted.length % 2) == 1) {    // odd length
            return sorted[sorted.length / 2];
        } else {    // even length
            return new Double((sorted[sorted.length / 2].doubleValue() + sorted[1 + sorted.length / 2].doubleValue()) / 2.0);
        }
    }

    public static Double[] add(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(arr[i].doubleValue() + v);
        }
        return res;
    }

    public static Double[] add(Number[] arr, Number val) {
        return add(arr, val);
    }

    public static Double[] add(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When adding two vectors, both must be of the same size!");
        }
        //
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() + arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] sub(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(v - arr[i].doubleValue());
        }
        return res;
    }

    public static Double[] sub(Number[] arr, Number val) {
        return sub(new Double(-val.doubleValue()), arr);
    }

    public static Double[] sub(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When subtracting two vectors, both must be of the same size!");
        }
        //
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() - arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] mul(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(v * arr[i].doubleValue());
        }
        return res;
    }

    public static Double[] mul(Number[] arr, Number val) {
        return mul(val, arr);
    }

    public static Double[] mul(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When multiplying two vectors, both must be of the same size!");
        }
        //
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() * arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] div(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(v / arr[i].doubleValue());
        }
        return res;
    }

    public static Double[] div(Number[] arr, Number val) {
        return mul(new Double(1.0 / val.doubleValue()), arr);
    }

    public static Double[] div(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When dividing two vectors (item-by-item), both must be of the same size!");
        }
        //
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() / arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] mod(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double tmp, v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            tmp = v / arr[i].doubleValue();
            res[i] = v - (((double) ((int) tmp)) * arr[i].doubleValue());
        }
        return res;
    }

    public static Double[] mod(Number[] arr, Number val) {
        Double[] res = new Double[arr.length];
        double tmp, v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            tmp = arr[i].doubleValue() / v;
            res[i] = arr[i].doubleValue() - (((double) ((int) tmp)) * v);
        }
        return res;
    }

    public static Double[] mod(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When performing modulo operation of two vectors (item-by-item), both must be of the same size!");
        }
        //
        Double[] res = new Double[arr1.length];
        double tmp;
        for(int i = 0; i < arr1.length; i++) {
            tmp = arr1[i].doubleValue() / arr2[i].doubleValue();
            res[i] = arr1[i].doubleValue() - (((double) ((int) tmp)) * arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] pow(Number[] arr, Number val) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(pow(arr[i].doubleValue(), v));
        }
        return res;
    }

    public static Double[] relEq(Double[] a, Double[] b) {
        int len1, len2;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
        } else {
            len1 = b.length;
            len2 = a.length;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = ((a[i].doubleValue() == b[i].doubleValue()) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = 0.0;
        }
        return res;
    }

    public static Double[] relGt(Double[] a, Double[] b) {
        int len1, len2;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
        } else {
            len1 = b.length;
            len2 = a.length;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = ((a[i].doubleValue() > b[i].doubleValue()) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = 0.0;
        }
        return res;
    }

    public static Double[] relLt(Double[] a, Double[] b) {
        int len1, len2;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
        } else {
            len1 = b.length;
            len2 = a.length;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = ((a[i].doubleValue() < b[i].doubleValue()) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = 0.0;
        }
        return res;
    }

    public static Double[] relLt(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v < vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relLt(Double[] vec, Double val) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((vec[i].doubleValue() < v) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relGt(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v > vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relGt(Double[] vec, Double val) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((vec[i].doubleValue() > v) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] logAnd(Double[] a, Double[] b) {
        int len1, len2;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
        } else {
            len1 = b.length;
            len2 = a.length;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = (((a[i].doubleValue() != 0.0) && (b[i].doubleValue() != 0.0)) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = 0.0;
        }
        return res;
    }

    public static Double[] logOr(Double[] a, Double[] b) {
        int len1, len2;
        Double[] larger;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
            larger = b;
        } else {
            len1 = b.length;
            len2 = a.length;
            larger = a;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = (((a[i].doubleValue() != 0.0) || (b[i].doubleValue() != 0.0)) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = larger[i];
        }
        return res;
    }

    public static Double[] relEq(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v == vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relEq(Double[] vec, Double val) {
        return relEq(val, vec);
    }

    public static Double abs(Double val) {
        return new Double(java.lang.Math.abs(val.doubleValue()));
    }

    public static Double[] abs(Double[] vec) {
        Double[] res = new Double[vec.length];
        for(int i = 0; i < vec.length; i++) {
            res[i] = abs(vec[i]);
        }
        return res;
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

    public static int min(int a, int b) {
        return FastMath.min(a, b);
    }

    public static double min(double a, double b) {
        return FastMath.min(a, b);
    }

    public static double max(double a, double b) {
        return FastMath.max(a, b);
    }
    
    public static int [] genIntSequence(int from, int length) {
        int [] seq = new int[length];
        for(int i = 0; i < length; i++) {
            seq[i] = from + i;
        }
        return seq;
    }
}