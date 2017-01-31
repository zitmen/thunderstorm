package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui

object DetectorFactory {

    @JvmStatic
    public fun createAllDetectorsUI()
            = arrayOf(NonMaxSuppressionDetectorUI())

    @JvmStatic
    public fun getDetectorByName(name: String)
            = createAllDetectorsUI().single { it.getName() == name }

    @JvmStatic
    public fun getDetectorIndexByName(name: String)
            = createAllDetectorsUI().withIndex().single { it.value.getName() == name }.index
}