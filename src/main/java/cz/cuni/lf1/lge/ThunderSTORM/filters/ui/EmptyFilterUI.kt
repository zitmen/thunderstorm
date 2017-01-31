package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.thunderstorm.algorithms.filters.Filter
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage

import javax.swing.JPanel

private const val name = "No filter"

public class EmptyFilterUI : FilterUI() {

    public override fun getName()
            = name

    public override fun getOptionsPanel(): JPanel
            = JPanel()

    public override fun getImplementation()
            = object : Filter {
                override fun filter(image: GrayScaleImage) = image
            }

    public override fun createThresholderSymbolTable(): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = mapOf(Pair("Empty.F", { img -> getImplementation().filter(img) }))
}