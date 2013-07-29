package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.results.TripleStateTableModel;

public class ProtoImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, TripleStateTableModel rt) {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.resetAll();
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportToFile(String fp, TripleStateTableModel rt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Google Protocol Buffer";
    }

}
