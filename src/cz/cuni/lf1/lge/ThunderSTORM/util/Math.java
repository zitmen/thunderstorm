package cz.cuni.lf1.lge.ThunderSTORM.util;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class Math {
    
    /**
     *
     */
    public static final double PI = org.apache.commons.math3.util.FastMath.PI;
    
    /**
     *
     * @param x
     * @param n
     * @return
     */
    public static double pow(double x, int n) {
        return org.apache.commons.math3.util.FastMath.pow(x, n);
    }
    
    /**
     *
     * @param x
     * @param n
     * @return
     */
    public static double pow(double x, double n) {
        return org.apache.commons.math3.util.FastMath.pow(x, n);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static double exp(double x) {
        return org.apache.commons.math3.util.FastMath.exp(x);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static double ceil(double x) {
        return org.apache.commons.math3.util.FastMath.ceil(x);
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    public static int max(int a, int b) {
        return org.apache.commons.math3.util.FastMath.max(a, b);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static long round(double x) {
        return org.apache.commons.math3.util.FastMath.round(x);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static int round(float x) {
        return org.apache.commons.math3.util.FastMath.round(x);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static double sqrt(double x) {
        return org.apache.commons.math3.util.FastMath.sqrt(x);
    }
    
    /**
     *
     * @param x
     * @return
     */
    public static double sqr(double x) {
        return x * x;
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static float sum(float [] arr) {
        float sum = 0.0f;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static double sum(double [] arr) {
        double sum = 0.0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static float mean(float [] arr) {
        return sum(arr) / (float) arr.length;
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static double mean(double [] arr) {
        return sum(arr) / (double) arr.length;
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static float stddev(float [] arr) {
        float sumdev = 0.0f, mean = mean(arr);
        for (int i = 0; i < arr.length; i++)
            sumdev += sqr(arr[i] - mean);
        return (float) sqrt(sumdev / (float) arr.length);
    }
    
    /**
     *
     * @param x
     * @param sigma
     * @param normalized
     * @return
     */
    public static double gauss(double x, double sigma, boolean normalized) {
        return exp(-0.5 * sqr(x / sigma)) / ((normalized) ? (sigma*sqrt(2*PI)) : 1);
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static float median(float [] arr) {
        return arr[arr.length/2];
    }
    
    /**
     *
     * @param arr
     * @return
     */
    public static double modus(double [] arr) {
        double [] sorted = java.util.Arrays.copyOf(arr, arr.length);
        java.util.Arrays.sort(arr);
        
        int max_i = 0, max_count = 0;
        for(int i = 1, count = 1; i < sorted.length; i++, count++) {
            if(sorted[i-1] != sorted[i]) {
                if(count > max_count) {
                    max_count = count;
                    max_i = i-1;
                }
                count = 0;
            }
        }
        
        return sorted[max_i];
    }
}
