package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;

public abstract class IPsfUI extends IModuleUI<PSFModel> {
    
    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".psf";
    }
    
}
