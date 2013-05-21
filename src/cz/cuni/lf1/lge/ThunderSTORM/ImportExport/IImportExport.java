package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import ij.measure.ResultsTable;
import java.io.IOException;

public interface IImportExport extends IModule {
    
    public String getName();
    public void importFromFile(String fp, ResultsTable rt) throws IOException;
    public void exportToFile(String fp, ResultsTable rt) throws IOException;

}
