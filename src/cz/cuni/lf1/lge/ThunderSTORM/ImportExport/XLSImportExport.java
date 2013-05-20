package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import javax.swing.JPanel;

public class XLSImportExport extends DLMImportExport implements IImportExport {

    public XLSImportExport() {
        super('\t');
    }
    
    @Override
    public String getName() {
        return "XLS (tab separated)";
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
