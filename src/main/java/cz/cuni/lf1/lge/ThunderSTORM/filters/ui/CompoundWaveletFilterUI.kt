package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.CompoundWaveletFilter
import cz.cuni.lf1.thunderstorm.algorithms.filters.WaveletFilter
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.algorithms.padding.ZeroPadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Wavelet filter (B-Spline)"

public class CompoundWaveletFilterUI : FilterUI() {

    private val order = parameters.createIntField("order", IntegerValidatorFactory.positiveNonZero(), 3)
    private val scale = parameters.createDoubleField("scale", DoubleValidatorFactory.positiveNonZero(), 2.0)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".wave"

    public override fun getOptionsPanel(): JPanel {
        val orderTextField = JTextField("", 20)
        val scaleTextField = JTextField("", 20)
        parameters.registerComponent(order, orderTextField)
        parameters.registerComponent(scale, scaleTextField)

        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("B-Spline order: "), GridBagHelper.leftCol())
        panel.add(orderTextField, GridBagHelper.rightCol())
        panel.add(JLabel("B-Spline scale: "), GridBagHelper.leftCol())
        panel.add(scaleTextField, GridBagHelper.rightCol())
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = CompoundWaveletFilter(
                order.value, scale.value,
                2*Math.ceil(order.value.toDouble()*scale.value/2.0).toInt() - 1,
                ::ZeroPadding)

    private fun getWaveletFilterImplementation(plane: Int)
            = WaveletFilter(
                plane, order.value, scale.value,
                2*Math.ceil(order.value.toDouble()*scale.value/2.0).toInt() - 1,
                ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(
                Pair("Wave.F1", { img -> getWaveletFilterImplementation(1).filter(img) }),
                Pair("Wave.F2", { img -> getWaveletFilterImplementation(2).filter(img) }))
}