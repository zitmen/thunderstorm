package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

public class XLSImportExport extends DLMImportExport implements IImportExport {

    public XLSImportExport() {
        super("\t");
    }
    
    @Override
    public String getName() {
        return "XLS (tab separated)";
    }

    @Override
    public String getSuffix() {
        return "xls";
    }

}
