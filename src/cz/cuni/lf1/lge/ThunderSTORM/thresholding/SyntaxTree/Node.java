package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;

public abstract class Node {
    
    public boolean isVariable(String filter, String var) {
        for(IFilterUI f : Thresholder.getLoadedFilters()) {
          IFilter impl = f.getImplementation();
            if(impl.getFilterVarName().equals(filter)) {
                return impl.exportVariables().containsKey(var);
            }
        }
        return false;
    }
    
    public RetVal getVariable(String filter, String var) {
        if(filter == null) {   // active filter
            return new RetVal(Thresholder.getActiveFilter().exportVariables().get(var));
        } else {    // the other ones
            for(IFilterUI f : Thresholder.getLoadedFilters()) {
              IFilter impl = f.getImplementation();
                if(impl.getFilterVarName().equals(filter)) {
                    return new RetVal(impl.exportVariables().get(var));
                }
            }
        }
        return new RetVal((FloatProcessor)null);
    }
    
    public abstract RetVal eval();
    public abstract void semanticScan() throws ThresholdFormulaException;

}
