package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI
import cz.cuni.lf1.thunderstorm.algorithms.detectors.Detector

public abstract class DetectorUI : ModuleUI<Detector>() {

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".detectors"

    public abstract fun getThresholdFormula(): String
}
