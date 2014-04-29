package cz.cuni.lf1.lge.ThunderSTORM.util;

import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.div;
import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.add;
import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.sum;

public class BSplines {
    
    /**
     * Gewnerate B-Spline shifted so it is symmetric around zero.
     * 
     * @param k order of b-spline
     * @param s scale of samples
     * @param t samples
     * @return the B-spline of order `k` sampled at points `t`
     */
    public static double [] bSplineBlender(int k, double s, double ... t) {
        return normalize(N(k, add(div(t, s), (double)k / 2.0)));   // scale and align to center, then evaluate, and finally normalize sum to 1
    }

    /**
     * The actual recursive blending function for generating B-Splines.
     * 
     * Note: although the algorithm could be optimized to call the recursion
     *       just once, it is not such issue, because in our application it is
     *       evaluated at only few spots
     * 
     * @param k order
     * @param t samples
     */
    private static double [] N(int k, double ... t) {
        if(k <= 1) {
            return haar(t);
        } else {
            double [] res = new double[t.length];
            for(int i = 0; i < t.length; i++) {
                double [] Nt = N(k-1,t[i]);
                double [] Nt_1 = N(k-1,t[i]-1);
                res[i] = t[i] / (k - 1) * Nt[0] + (k - t[i]) / (k - 1) * Nt_1[0];
            }
            return res;
        }
    }
    
    /**
     * Generate Haar basis (no scaling).
     */
    private static double [] haar(double ... t) {
        double [] res = new double[t.length];
        for(int i = 0; i < t.length; i++) {
            res[i] = ((t[i] >= 0 && t[i] < 1) ? 1 : 0);
        }
        return res;
    }

    /**
     * Normalize sum to 1.
     */
    private static double[] normalize(double[] arr) {
        return div(arr, sum(arr));
    }
    
}
