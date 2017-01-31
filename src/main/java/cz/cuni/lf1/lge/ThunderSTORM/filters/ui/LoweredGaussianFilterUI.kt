package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.LoweredGaussianFilter
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Lowered Gaussian filter"

public class LoweredGaussianFilterUI : FilterUI() {

    private val sigma = parameters.createDoubleField("sigma", DoubleValidatorFactory.positiveNonZero(), 1.6)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".lowgauss"

    public override fun getOptionsPanel(): JPanel {
        val sigmaTextField = JTextField("", 20)
        parameters.registerComponent(sigma, sigmaTextField)

        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("Sigma [px]: "), GridBagHelper.leftCol())
        panel.add(sigmaTextField, GridBagHelper.rightCol())
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = LoweredGaussianFilter(1 + 2*Math.ceil(3.0*sigma.value).toInt(), sigma.value, ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(Pair("LowGauss.F", { img -> getImplementation().filter(img) }))
}