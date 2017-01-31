package cz.cuni.lf1.lge.ThunderSTORM.calibration

object DefocusFunctionFactory {

    @JvmStatic
    public fun createAllDefocusFunctions()
            = arrayOf(
                DefocusFunctionPoly(),
                DefocusFunctionSqrt())

    @JvmStatic
    public fun getDefocusFunctionByName(name: String)
            = createAllDefocusFunctions().single { it.getName() == name }
}