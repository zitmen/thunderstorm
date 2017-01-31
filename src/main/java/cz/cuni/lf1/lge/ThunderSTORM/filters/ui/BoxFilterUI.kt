package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.BoxFilter
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage

import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Averaging (Box) filter"

public class BoxFilterUI : FilterUI() {

    private val size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 3)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".box"

    public override fun getOptionsPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val sizeTextField = JTextField("", 20)
        parameters.registerComponent(size, sizeTextField)

        panel.add(JLabel("Kernel size [px]: "), GridBagHelper.leftCol())
        panel.add(sizeTextField, GridBagHelper.rightCol())
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = BoxFilter(size.value, ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(Pair("Box.F", { img -> getImplementation().filter(img) }))
}
