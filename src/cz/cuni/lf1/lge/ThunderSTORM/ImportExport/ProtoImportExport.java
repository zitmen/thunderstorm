package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import ij.measure.ResultsTable;
import javax.swing.JPanel;

public class ProtoImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, ResultsTable rt) {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportToFile(String fp, ResultsTable rt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Google Protocol Buffer";
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
