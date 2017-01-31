package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui

object PsfFactory {

    @JvmStatic
    public fun createAllPsfUI()
            = arrayOf(
                IntegratedSymmetricGaussianUI(),
                SymmetricGaussianUI(),
                EllipticGaussianWAngleUI())
}