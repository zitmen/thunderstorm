package cz.cuni.lf1.lge.ThunderSTORM.ImportExport

public object ImportExportFactory {

    @JvmStatic
    public fun createAllImportExportModules()
            = arrayOf(
                CSVImportExport(),
                XLSImportExport(),
                XMLImportExport(),
                JSONImportExport(),
                YAMLImportExport(),
                ProtoImportExport(),
                TSFImportExport())
}