package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.results.TripleStateTableModel;
import java.io.IOException;

public interface IImportExport extends IModule {
    
    public String getName();
    public void importFromFile(String fp, TripleStateTableModel rt) throws IOException;
    public void exportToFile(String fp, TripleStateTableModel rt) throws IOException;

}
