package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod
import ij.ImagePlus
import javax.swing.JPanel

public class EmptyRendererUI : AbstractRenderingUI() {
    init {
        parameters.loadPrefs()
    }

    public override fun getName()
            = "No Renderer"

    public override fun getOptionsPanel(): JPanel
            = JPanel()

    override fun getMethod(): IncrementalRenderingMethod {
        return object : IncrementalRenderingMethod {
            override fun addToImage(x: DoubleArray?, y: DoubleArray?, z: DoubleArray?, dx: DoubleArray?, dz: DoubleArray?) {}
            override fun addToImage(fits: MutableList<Molecule>?) {}
            override fun getRenderedImage() = ImagePlus()
            override fun getRendererName() = ""
            override fun reset() {}
        }
    }
}
