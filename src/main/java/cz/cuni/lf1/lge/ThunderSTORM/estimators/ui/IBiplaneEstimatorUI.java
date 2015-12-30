package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IBiplaneEstimator;

public abstract class IBiplaneEstimatorUI extends IModuleUI<IBiplaneEstimator> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".estimators.biplane";
    }
}
