package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import javax.swing.JPanel;

public class CSVImportExport extends DLMImportExport implements IImportExport {

    public CSVImportExport() {
        super(',');
    }

    @Override
    public String getName() {
        return "CSV (comma separated)";
    }

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public void readParameters() {
        //
    }

}
