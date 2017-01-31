package cz.cuni.lf1.lge.ThunderSTORM.filters.ui

import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage

object FilterFactory {

    @JvmStatic
    public fun createAllFiltersUI()
            = arrayOf(
                CompoundWaveletFilterUI(),
                BoxFilterUI(),
                DifferenceOfBoxFiltersUI(),
                GaussianFilterUI(),
                LoweredGaussianFilterUI(),
                DifferenceOfGaussiansFilterUI(),
                MedianFilterUI(),
                EmptyFilterUI())

    @JvmStatic
    public fun createThresholderSymbolTable(filters: Array<FilterUI>, activeFilterIndex: Int): Map<String, (GrayScaleImage) -> GrayScaleImage>
            = (filters.flatMap { f -> f.createThresholderSymbolTable().map { entry -> Pair(entry.key, entry.value) } }
                + arrayOf(Pair("I", { img -> img }), Pair("F", { img -> filters[activeFilterIndex].getImplementation().filter(img) })))
                .toMap()

    @JvmStatic
    public fun getFilterByName(name: String)
            = createAllFiltersUI().single { it.getName() == name }

    @JvmStatic
    public fun getFilterIndexByName(name: String)
            = createAllFiltersUI().withIndex().single { it.value.getName() == name }.index
}