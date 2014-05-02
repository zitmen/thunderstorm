package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;

public abstract class IFilterUI extends IModuleUI<IFilter> {

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".filters";
    }
    //
    
}
