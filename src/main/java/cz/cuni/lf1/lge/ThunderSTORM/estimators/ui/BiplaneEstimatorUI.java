package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

public abstract class BiplaneEstimatorUI extends BaseEstimatorUI {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".biplane";
    }
}
