package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;

public abstract class BaseEstimatorUI extends ModuleUI<IModule> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".estimators";
    }
}
