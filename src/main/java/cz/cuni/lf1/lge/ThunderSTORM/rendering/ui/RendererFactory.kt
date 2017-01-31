package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui

public object RendererFactory {

    @JvmStatic
    public fun createAllRenderers()
            = arrayOf(
                ASHRenderingUI(),
                ScatterRenderingUI(),
                DensityRenderingUI(),
                HistogramRenderingUI(),
                EmptyRendererUI())

    @JvmStatic
    public fun getRendererByName(name: String)
            = createAllRenderers().single { it.getName() == name }
}