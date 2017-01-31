package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI;
import cz.cuni.lf1.thunderstorm.algorithms.filters.Filter
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage

public abstract class FilterUI : ModuleUI<Filter>() {

    override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".filters"

    public abstract fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
}
