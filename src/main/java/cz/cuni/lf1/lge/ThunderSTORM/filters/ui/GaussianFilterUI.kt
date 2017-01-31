package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Gaussian filter"

public class GaussianFilterUI : FilterUI() {

    private val sigma = parameters.createDoubleField("sigma", DoubleValidatorFactory.positiveNonZero(), 1.6)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".gauss"

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
            = GaussianFilter(1 + 2*Math.ceil(3.0*sigma.value).toInt(), sigma.value, ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(Pair("Gauss.F", { img -> getImplementation().filter(img) }))
}
