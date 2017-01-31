package cz.cuni.lf1.lge.ThunderSTORM.results

public object PostProcessingModulesFactory {

    @JvmStatic
    public fun createAllPostProcessingModules()
            = arrayOf(
                ResultsFilter(),
                LocalDensityFilter(),
                DuplicatesFilter(),
                ResultsGrouping(),
                ResultsDriftCorrection(),
                ResultsStageOffset())
}