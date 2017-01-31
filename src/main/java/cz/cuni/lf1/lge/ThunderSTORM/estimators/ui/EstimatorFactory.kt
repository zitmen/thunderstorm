package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui

import cz.cuni.lf1.lge.ThunderSTORM.estimators.EmptyEstimator

object EstimatorFactory {

    @JvmStatic
    public fun createAllBiPlaneEstimatorsUI()
            = arrayOf(
                EllipticBiplaneGaussianEstimatorUI())

    @JvmStatic
    public fun createAllEstimatorsUI()
            = arrayOf(
                IntSymmetricGaussianEstimatorUI(),
                SymmetricGaussianEstimatorUI(),
                EllipticGaussianEstimatorUI(),
                RadialSymmetryEstimatorUI(),
                CenterOfMassEstimatorUI(),
                EmptyEstimator())

    @JvmStatic
    public fun getEstimatorByName(name: String)
            = createAllEstimatorsUI().single { it.getName() == name }

    @JvmStatic
    public fun getBiPlaneEstimatorByName(name: String)
            = createAllBiPlaneEstimatorsUI().single { it.getName() == name }
}