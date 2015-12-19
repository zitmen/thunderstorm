package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

public class CSVImportExport extends DLMImportExport implements IImportExport {

    public CSVImportExport() {
        super(",");
    }

    @Override
    public String getName() {
        return "CSV (comma separated)";
    }

    @Override
    public String getSuffix() {
        return "csv";
    }

}
