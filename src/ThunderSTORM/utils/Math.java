package ThunderSTORM.utils;

import static java.lang.Math.sqrt;

// TODO: transform these methods to generics, e.g., public static <T extends Number> T sqr(T x) { return x*x; }
//       --> the problem is that T cannot be int or double like in C++, but it has to be Integer or Double, ...
//       --> why autoboxing does not work? all examples on the internet are just with Integer class
public class Math {

    public static double sqr(double x) {
        return x * x;
    }
    
    public static double sum(double [] arr) {
        double sum = 0.0;
        for (int i = 0; i < arr.length; i++)
            sum += arr[i];
        return sum;
    }
    
    public static double mean(double [] arr) {
        return sum(arr) / (double) arr.length;
    }
    
    public static double stddev(double [] arr) {
        double sumdev = 0.0, mean = mean(arr);
        for (int i = 0; i < arr.length; i++)
            sumdev += sqr(arr[i] - mean);
        return sqrt(sumdev / (double) arr.length);
    }
}
