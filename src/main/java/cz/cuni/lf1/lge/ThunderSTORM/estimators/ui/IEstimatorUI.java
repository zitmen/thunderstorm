package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;

public abstract class IEstimatorUI extends IModuleUI<IEstimator> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".estimators";
    }
}
