package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;

public abstract class IDetectorUI extends IModuleUI<IDetector> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".detectors";
    }
    //
    
}
