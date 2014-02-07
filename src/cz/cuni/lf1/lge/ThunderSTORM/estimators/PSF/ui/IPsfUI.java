package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;

public abstract class IPsfUI extends IModuleUI<PSFModel> {
    
    public static double fwhm2sigma(double fwhm) {
        return fwhm / 2.355;
    }
    
    public static double sigma2fwhm(double sigma) {
        return 2.355 * sigma;
    }
    
    @Override
    protected String getPreferencesPrefix() {
        return "thunderstorm.datagen.psf";
    }
    
    abstract public double getAngle();
    abstract public Range getZRange();
    abstract public double getSigma1(double z);
    abstract public double getSigma2(double z);
    
}
