package cz.cuni.lf1.lge.ThunderSTORM.util

import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.div
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.add
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.sum

object BSplines {

    /**
     * Gewnerate B-Spline shifted so it is symmetric around zero.

     * @param k order of b-spline
     * *
     * @param s scale of samples
     * *
     * @param t samples
     * *
     * @return the B-spline of order `k` sampled at points `t`
     */
    fun bSplineBlender(k: Int, s: Double, vararg t: Double): DoubleArray {
        return normalize(N(k, *add(div(t, s), k.toDouble() / 2.0)))   // scale and align to center, then evaluate, and finally normalize sum to 1
    }

    /**
     * The actual recursive blending function for generating B-Splines.

     * Note: although the algorithm could be optimized to call the recursion
     * just once, it is not such issue, because in our application it is
     * evaluated at only few spots

     * @param k order
     * *
     * @param t samples
     */
    private fun N(k: Int, vararg t: Double): DoubleArray {
        if (k <= 1) {
            return haar(*t)
        } else {
            val res = DoubleArray(t.size)
            for (i in t.indices) {
                val Nt = N(k - 1, t[i])
                val Nt_1 = N(k - 1, t[i] - 1)
                res[i] = t[i] / (k - 1) * Nt[0] + (k - t[i]) / (k - 1) * Nt_1[0]
            }
            return res
        }
    }

    /**
     * Generate Haar basis (no scaling).
     */
    private fun haar(vararg t: Double): DoubleArray {
        val res = DoubleArray(t.size)
        for (i in t.indices) {
            res[i] = (if (t[i] >= 0 && t[i] < 1) 1 else 0).toDouble()
        }
        return res
    }

    /**
     * Normalize sum to 1.
     */
    private fun normalize(arr: DoubleArray): DoubleArray {
        return div(arr, sum(arr))
    }

}
