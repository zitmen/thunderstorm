package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory
import cz.cuni.lf1.thunderstorm.algorithms.filters.BoxFilter
import cz.cuni.lf1.thunderstorm.algorithms.filters.DifferenceOfBoxesFilters
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

private const val name = "Difference of averaging filters"

public class DifferenceOfBoxFiltersUI : FilterUI() {

    private val size1 = parameters.createIntField("size1", IntegerValidatorFactory.positiveNonZero(), 3)
    private val size2 = parameters.createIntField("size2", IntegerValidatorFactory.positiveNonZero(), 5)

    public override fun getName()
            = name

    protected override fun getPreferencesPrefix()
            = super.getPreferencesPrefix() + ".dob"

    public override fun getOptionsPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val sizeTextField1 = JTextField("", 20)
        val sizeTextField2 = JTextField("", 20)
        parameters.registerComponent(size1, sizeTextField1)
        parameters.registerComponent(size2, sizeTextField2)

        panel.add(JLabel("First kernel size [px]: "), GridBagHelper.leftCol())
        panel.add(sizeTextField1, GridBagHelper.rightCol())
        panel.add(JLabel("Second kernel size [px]: "), GridBagHelper.leftCol())
        panel.add(sizeTextField2, GridBagHelper.rightCol())
        parameters.loadPrefs()
        return panel
    }

    public override fun getImplementation()
            = DifferenceOfBoxesFilters(size1.value, size2.value, ::DuplicatePadding)

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(
                Pair("DoB.B1", { img -> BoxFilter(size1.value, ::DuplicatePadding).filter(img) }),
                Pair("DoB.B2", { img -> BoxFilter(size2.value, ::DuplicatePadding).filter(img) }))
}
