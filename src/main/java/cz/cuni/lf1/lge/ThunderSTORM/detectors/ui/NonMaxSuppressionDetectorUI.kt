package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.detectors.MaxFilterDetector
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Maximum filter"

public class NonMaxSuppressionDetectorUI : DetectorUI() {

    private val radius = parameters.createIntField("radius", IntegerValidatorFactory.positiveNonZero(), 1)
    private val threshold = parameters.createStringField("threshold", null, "std(Wave.F1)")

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".nonmaxsup"

    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel {
        val thrTextField = JTextField("", 20)
        val radiusTextField = JTextField("", 20)
        parameters.registerComponent(threshold, thrTextField)
        parameters.registerComponent(radius, radiusTextField)

        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("Peak intensity threshold: "), GridBagHelper.leftCol())
        panel.add(thrTextField, GridBagHelper.rightCol())
        panel.add(JLabel("Dilation radius [px]: "), GridBagHelper.leftCol())
        panel.add(radiusTextField, GridBagHelper.rightCol())

        parameters.loadPrefs()
        return panel
    }

    public override fun getThresholdFormula()
            = threshold.value

    public override fun getImplementation()
            = MaxFilterDetector(radius.value)
}