package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.log
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt
import cz.cuni.lf1.lge.ThunderSTORM.util.Range

public abstract class PsfUI : ModuleUI<PSFModel>() {

    companion object {
        private val FWHM_FACTOR = 2.0*sqrt(2.0*log(2.0))
        public fun fwhm2sigma(fwhm: Double)= fwhm / FWHM_FACTOR
        public fun sigma2fwhm(sigma: Double) = FWHM_FACTOR * sigma
    }

    protected override fun getPreferencesPrefix()
            = "thunderstorm.datagen.psf"
    
    public abstract fun getAngle(): Double
    public abstract fun getZRange(): Range
    public abstract fun getSigma1(z: Double): Double
    public abstract fun getSigma2(z: Double): Double
    public abstract fun is3D(): Boolean
    
}
