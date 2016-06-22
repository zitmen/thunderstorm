package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import java.io.IOException;
import java.util.List;

public interface IImportExport extends IModule {
    
    public String getName();
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException;
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws IOException;
    public String getSuffix();  // filename suffix
   
}
