package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import java.util.Arrays;

public class VectorMath {

    public static Double[] relEq(Double[] vec, Double val) {
        return relEq(val, vec);
    }

    public static Double[] relEq(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v == vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relEq(Double[] a, Double[] b) {
        int len1;
        int len2;
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

    public static Double sum(Number[] arr) {
        double sum = 0.0;
        for(int i = 0; i < arr.length; i++) {
            sum += arr[i].doubleValue();
        }
        return new Double(sum);
    }

    public static Double[] relLt(Double[] vec, Double val) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((vec[i].doubleValue() < v) ? 1.0 : 0.0);
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

    public static Double[] relLt(Double[] a, Double[] b) {
        int len1;
        int len2;
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

    public static Double[] relNeq(Double[] vec, Double val) {
        return relNeq(val, vec);
    }

    public static Double[] relNeq(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v != vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relNeq(Double[] a, Double[] b) {
        int len1;
        int len2;
        if(a.length < b.length) {
            len1 = a.length;
            len2 = b.length;
        } else {
            len1 = b.length;
            len2 = a.length;
        }
        Double[] res = new Double[len2];
        for(int i = 0; i < len1; i++) {
            res[i] = ((a[i].doubleValue() != b[i].doubleValue()) ? 1.0 : 0.0);
        }
        for(int i = len1; i < len2; i++) {
            res[i] = 0.0;
        }
        return res;
    }

    public static Double[] mod(Number val, Number[] arr) {
        Double[] res = new Double[arr.length];
        double tmp;
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            tmp = v / arr[i].doubleValue();
            res[i] = v - (((double) ((int) tmp)) * arr[i].doubleValue());
        }
        return res;
    }

    public static Double[] mod(Number[] arr, Number val) {
        Double[] res = new Double[arr.length];
        double tmp;
        double v = val.doubleValue();
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
        Double[] res = new Double[arr1.length];
        double tmp;
        for(int i = 0; i < arr1.length; i++) {
            tmp = arr1[i].doubleValue() / arr2[i].doubleValue();
            res[i] = arr1[i].doubleValue() - (((double) ((int) tmp)) * arr2[i].doubleValue());
        }
        return res;
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

    public static Double[] div(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When dividing two vectors (item-by-item), both must be of the same size!");
        }
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() / arr2[i].doubleValue());
        }
        return res;
    }

    public static Double[] pow(Number[] arr, Number val) {
        Double[] res = new Double[arr.length];
        double v = val.doubleValue();
        for(int i = 0; i < arr.length; i++) {
            res[i] = new Double(MathProxy.pow(arr[i].doubleValue(), v));
        }
        return res;
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

    public static Double[] logOr(Double[] a, Double[] b) {
        int len1;
        int len2;
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

    public static Double[] add(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When adding two vectors, both must be of the same size!");
        }
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() + arr2[i].doubleValue());
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

    /**
     * Compute the standard deviation of an array of floats.
     *
     * @param arr an array of floats
     * @return {
     * @mathjax \sqrt{\frac{1}{arr.length} \sum_{i=0}^{arr.length}{\left(arr[i]
     * - \mu\right)}}}, where {
     * @mathjax \mu} = {@code mean(arr)}.
     */
    public static float stddev(float[] arr) {
        float sumdev = 0.0F;
        float mean = mean(arr);
        for(int i = 0; i < arr.length; i++) {
            sumdev += MathProxy.sqr(arr[i] - mean);
        }
        return (float) MathProxy.sqrt(sumdev / (float) arr.length);
    }

    public static Double stddev(Number[] arr) {
        double sumdev = 0.0;
        double mean = mean(arr);
        for(int i = 0; i < arr.length; i++) {
            sumdev += MathProxy.sqr(arr[i].doubleValue() - mean);
        }
        return new Double(MathProxy.sqrt(sumdev / (double) arr.length));
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
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() * arr2[i].doubleValue());
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

    public static Double[] relGt(Double val, Double[] vec) {
        Double[] res = new Double[vec.length];
        double v = val.doubleValue();
        for(int i = 0; i < vec.length; i++) {
            res[i] = ((v > vec[i].doubleValue()) ? 1.0 : 0.0);
        }
        return res;
    }

    public static Double[] relGt(Double[] a, Double[] b) {
        int len1;
        int len2;
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

    public static Double[] logAnd(Double[] a, Double[] b) {
        int len1;
        int len2;
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

    /**
     * Compute the mean value of an array of floats.
     *
     * @param arr an array of floats
     * @return {
     * @mathjax \frac{1}{arr.length} \sum_{i=0}^{arr.length}{arr[i]}}
     */
    public static float mean(float[] arr) {
        return sum(arr) / (float) arr.length;
    }

    public static Double mean(Number[] arr) {
        return new Double(sum(arr).doubleValue() / (double) arr.length);
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

    public static Double[] sub(Number[] arr1, Number[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When subtracting two vectors, both must be of the same size!");
        }
        Double[] res = new Double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = new Double(arr1[i].doubleValue() - arr2[i].doubleValue());
        }
        return res;
    }

    public static double[] sub(double[] arr1, double[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When subtracting two vectors, both must be of the same size!");
        }
        double[] res = new double[arr1.length];
        for(int i = 0; i < arr1.length; i++) {
            res[i] = arr1[i] - arr2[i];
        }
        return res;
    }

    public static double[] sub(double[] res, double[] arr1, double[] arr2) {
        if(arr1.length != arr2.length) {
            throw new FormulaParserException("When subtracting two vectors, both must be of the same size!");
        }
        if((res == null) || (res.length < arr1.length)) {
            res = new double[arr1.length];
        }
        for(int i = 0; i < arr1.length; i++) {
            res[i] = arr1[i] - arr2[i];
        }
        return res;
    }
    
    public static double[] addScalar(double[] arr, double scalar){
        double[] ret = arr.clone();
        return addScalarInPlace(ret, scalar);
    }
    
    public static double[] addScalarInPlace(double[] arr, double scalar){
        for(int i = 0; i < arr.length; i++) {
            arr[i] = arr[i] + scalar;
        }
        return arr;
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
