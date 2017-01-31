package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.DifferenceOfGaussiansFilter
import cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Difference-of-Gaussians filter"

public class DifferenceOfGaussiansFilterUI : FilterUI() {

    private val sigmaG1 = parameters.createDoubleField("sigma1", DoubleValidatorFactory.positiveNonZero(), 1.0)
    private val sigmaG2 = parameters.createDoubleField("sigma2", DoubleValidatorFactory.positiveNonZero(), 1.6)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".dog"

    public override fun getOptionsPanel(): JPanel {
        val sigma1TextField = JTextField("", 20)
        val sigma2TextField = JTextField("", 20)
        parameters.registerComponent(sigmaG1, sigma1TextField)
        parameters.registerComponent(sigmaG2, sigma2TextField)

        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("Sigma1 [px]: "), GridBagHelper.leftCol())
        panel.add(sigma1TextField, GridBagHelper.rightCol())
        panel.add(JLabel("Sigma2 [px]: "), GridBagHelper.leftCol())
        panel.add(sigma2TextField, GridBagHelper.rightCol())
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = DifferenceOfGaussiansFilter(
                1 + 2*Math.ceil(3.0*Math.max(sigmaG1.value, sigmaG2.value)).toInt(),
                sigmaG1.value, sigmaG2.value, ::DuplicatePadding)

    private fun getGaussianFilterImplementation(sigma: Double)
            = GaussianFilter(1 + 2*Math.ceil(3.0*sigma).toInt(), sigma, ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(
                Pair("DoG.G1", { img -> getGaussianFilterImplementation(sigmaG1.value).filter(img) }),
                Pair("DoG.G2", { img -> getGaussianFilterImplementation(sigmaG2.value).filter(img) }))
}