package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import static org.apache.commons.math3.util.FastMath.log;

public abstract class IPsfUI extends IModuleUI<PSFModel> {
    
    public static final double FWHM_FACTOR = 2*sqrt(2*log(2));
    
    public static double fwhm2sigma(double fwhm) {
        return fwhm / FWHM_FACTOR;
    }
    
    public static double sigma2fwhm(double sigma) {
        return FWHM_FACTOR * sigma;
    }
    
    @Override
    protected String getPreferencesPrefix() {
        return "thunderstorm.datagen.psf";
    }
    
    abstract public double getAngle();
    abstract public Range getZRange();
    abstract public double getSigma1(double z);
    abstract public double getSigma2(double z);
    abstract public boolean is3D();
    
}
