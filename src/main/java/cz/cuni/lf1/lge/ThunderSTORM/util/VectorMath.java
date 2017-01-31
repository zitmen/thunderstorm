package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.Arrays;

public class VectorMath {

    /**
     * Compute the sum of an array of doubles.
     *
     * @param arr an array of doubles
     * @return {
     * @mathjax \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double sum(double[] arr) {
        double sum = 0.0;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    /**
     * Compute the sum of an array of floats.
     *
     * @param arr an array of floats
     * @return {
     * @mathjax \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static float sum(float[] arr) {
        float sum = 0.0F;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public static void cumulativeSum(double[] arr, boolean reverseDirection) {
        double sum = 0;
        if(!reverseDirection) {
            for(int i = 0; i < arr.length; i++) {
                sum += arr[i];
                arr[i] = sum;
            }
        } else {
            for(int i = arr.length - 1; i >= 0; i--) {
                sum += arr[i];
                arr[i] = sum;
            }
        }
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
        if((sorted.length % 2) == 1) {
            return sorted[sorted.length / 2];
        } else {
            return (sorted[sorted.length / 2] + sorted[1 + sorted.length / 2]) / 2.0F;
        }
    }

    public static Number median(Number[] arr) {
        Number[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);
        if((sorted.length % 2) == 1) {
            return sorted[sorted.length / 2];
        } else {
            return new Double((sorted[sorted.length / 2].doubleValue() + sorted[1 + sorted.length / 2].doubleValue()) / 2.0);
        }
    }

    public static int max(int[] array) {
        int max = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i] > max) {
                max = array[i];
            }
        }
        return max;
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

    public static Number max(Number[] array) {
        Number max = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i].doubleValue() > max.doubleValue()) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * Compute the variance of an array of doubles.
     *
     * @param arr an array of doubles
     * @return statistical variance
     */
    public static double var(double[] arr) {
        double sumdev = 0.0;
        double mean = mean(arr);
        for(int i = 0; i < arr.length; i++) {
            sumdev += MathProxy.sqr(arr[i] - mean);
        }
        return sumdev / (double) arr.length;
    }
    
    public static double [] div(double [] arr, double val) {
        double[] res = new double[arr.length];
        for(int i = 0; i < arr.length; i++) {
            res[i] = arr[i] / val;
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

    public static float min(float[] array) {
        float min = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i] < min) {
                min = array[i];
            }
        }
        return min;
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

    public static Number min(Number[] array) {
        Number min = array[0];
        for(int i = 0; i < array.length; i++) {
            if(array[i].doubleValue() < min.doubleValue()) {
                min = array[i];
            }
        }
        return min;
    }

    public static double[] add(double[] arr, double scalar) {
        if(scalar != 0) {
            for(int i = 0; i < arr.length; i++) {
                arr[i] += scalar;
            }
        }
        return arr;
    }

    public static Double[] add(Number[] arr, Number val) {
        Double[] res = new Double[arr.length];
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(arr[i].doubleValue() + val.doubleValue());
        }
        return res;
    }

    public static Double[] abs(Double[] vec) {
        Double[] res = new Double[vec.length];
        for(int i = 0; i < vec.length; i++) {
            res[i] = MathProxy.abs(vec[i]);
        }
        return res;
    }

    /**
     * Compute the standard deviation of an array of doubles.
     *
     * @param arr an array of doubles
     * @return {
     * @mathjax \sqrt{\frac{1}{arr.length} \sum_{i=0}^{arr.length}{\left(arr[i]
     * - \mu\right)}}}, where {
     * @mathjax \mu} = {@code mean(arr)}.
     */
    public static double stddev(double[] arr) {
        return MathProxy.sqrt(var(arr));
    }

    public static Double[] mul(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(v * arr[i].doubleValue());
        }
        return res;
    }

    /**
     * Compute the mean value of an array of doubles.
     *
     * @param arr an array of doubles
     * @return {
     * @mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static double mean(double[] arr) {
        return sum(arr) / (double) arr.length;
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
        return add(arr, new Double(-val.doubleValue()));
    }

    public static double[] sub(double[] arr1, double[] arr2) {
        if(arr1.length != arr2.length) {
            throw new IllegalArgumentException("When subtracting two vectors, both must be of the same size!");
        }
        double[] res = new double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = arr1[i] - arr2[i];
        }
        return res;
    }

    public static double[] movingAverage(double[] values, int lag) {
        if (lag <= 0 || lag % 2 == 0) {
            throw new IllegalArgumentException("`lag` must be a positive odd number!");
        }
        double[] mavg = new double[values.length];
        // moving sum
        mavg[0] = 0.0;
        for (int i = 0, l = lag / 2; i <= l && i < values.length; i++) {
            mavg[0] += values[i];
        }
        for (int i = 1, l = lag / 2, sub = i - l - 1, add = i + l; i < mavg.length; i++, sub++, add++) {
            mavg[i] = mavg[i-1];
            if (sub >= 0) mavg[i] -= values[sub];
            if (add < values.length) mavg[i] += values[add];
        }
        // moving average
        for (int i = 0, j = mavg.length - 1, l = lag / 2; i < mavg.length; i++, j--) {
            double count = (double) Math.min(lag - Math.max(Math.max(0, l - i), Math.max(0, l - j)), mavg.length);
            mavg[i] /= count;
        }
        return mavg;
    }
}
