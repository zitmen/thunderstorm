package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.MedianFilter
import cz.cuni.lf1.thunderstorm.algorithms.filters.MedianFilterPattern
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.*

private const val name = "Median filter"

private const val box = "box"
private const val cross = "cross"

public class MedianFilterUI : FilterUI() {

    private val size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 3)
    private val pattern = parameters.createStringField("pattern", null, box)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".median"

    public override fun getOptionsPanel(): JPanel {
        val btnGroup = ButtonGroup()
        val patternBoxRadioButton = JRadioButton(box)
        val patternCrossRadioButton = JRadioButton(cross)
        btnGroup.add(patternBoxRadioButton)
        btnGroup.add(patternCrossRadioButton)
        val sizeTextField = JTextField("", 20)
        parameters.registerComponent(size, sizeTextField)
        parameters.registerComponent(pattern, btnGroup)

        val panel = JPanel(GridBagLayout())
        panel.add(JLabel("Kernel size [px]: "), GridBagHelper.leftCol())
        panel.add(sizeTextField, GridBagHelper.rightCol())
        panel.add(JLabel("Pattern: "), GridBagHelper.leftCol())
        panel.add(patternBoxRadioButton, GridBagHelper.rightCol())
        panel.add(patternCrossRadioButton, GridBagHelper.rightCol())

        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = MedianFilter(if (pattern.value == box) MedianFilterPattern.BOX else MedianFilterPattern.CROSS, size.value)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(Pair("Med.F", { img -> getImplementation().filter(img) }))
}
