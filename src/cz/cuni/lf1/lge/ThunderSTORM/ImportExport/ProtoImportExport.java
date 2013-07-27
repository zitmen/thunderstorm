package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import java.util.Vector;

public class ProtoImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, IJResultsTable rt) {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportToFile(String fp, IJResultsTable.View rt, Vector<String> columns) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Google Protocol Buffer";
    }

    @Override
    public String getSuffix() {
        return "proto";
    }

}
