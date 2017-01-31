package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ScatterRendering
import cz.cuni.lf1.lge.ThunderSTORM.util.Range
import javax.swing.JPanel

public class ScatterRenderingUI(sizeX: Double = 0.0, sizeY: Double = 0.0) : AbstractRenderingUI(sizeX, sizeY) {

    companion object {
        public const val name = "Scatter plot"
    }

    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel {
        val p = super.getOptionsPanel()
        parameters.loadPrefs()
        return p
    }

    public override fun getMethod(): IncrementalRenderingMethod {
        if(threeD!!.getValue()) {
            val zrange = Range.parseFromStepTo(zRange!!.getValue())
            return ScatterRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .colorize(colorize!!.getValue())
                    .colorizationLUT(lutPicker!!.getLut(colorizationLut!!.getValue()))
                    .zRange(zrange.from, zrange.to, zrange.step)
                    .build()
        } else {
            return ScatterRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification!!.getValue())
                    .build()
        }
    }
}
