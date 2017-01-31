package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod

public abstract class RendererUI : ModuleUI<IncrementalRenderingMethod>() {

    public abstract fun setSize(sizeX: Double, sizeY: Double)
    public abstract fun setSize(left: Double, top: Double, sizeX: Double, sizeY: Double)
    public abstract fun setZRange(from: Double, to: Double)
    public abstract fun set3D(checked: Boolean)

    public abstract fun getRepaintFrequency(): Int

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".rendering"
}