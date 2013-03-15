package ThunderSTORM.utils;

import static java.lang.Math.sqrt;
import static java.lang.Math.exp;
import static java.lang.Math.PI;
import java.util.Arrays;

// TODO: transform these methods to generics, e.g., public static <T extends Number> T sqr(T x) { return x*x; }
//       --> the problem is that T cannot be int or double like in C++, but it has to be Integer or Double, ...
//       --> why autoboxing does not work? all examples on the internet are just with Integer class
public class Math {

    public static double sqr(double x) {
        return x * x;
    }
    
    public static float sum(float [] arr) {
        float sum = 0.0f;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    public static double sum(double [] arr) {
        double sum = 0.0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    public static float mean(float [] arr) {
        return sum(arr) / (float) arr.length;
    }
    
    public static double mean(double [] arr) {
        return sum(arr) / (double) arr.length;
    }
    
    public static float stddev(float [] arr) {
        float sumdev = 0.0f, mean = mean(arr);
        for (int i = 0; i < arr.length; i++)
            sumdev += sqr(arr[i] - mean);
        return (float) sqrt(sumdev / (float) arr.length);
    }
    
    public static double gauss(double x, double sigma, boolean normalized) {
        return exp(-0.5 * sqr(x / sigma)) / ((normalized) ? (sigma*sqrt(2*PI)) : 1);
    }
    
    public static float median(float [] arr) {
        return arr[arr.length/2];
    }
    
    public static double modus(double [] arr) {
        double [] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(arr);
        
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
