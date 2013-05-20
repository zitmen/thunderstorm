package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.ThreadLocalModule;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;

public abstract class Node {
    
    public boolean isVariable(String filter, String var) {
        for(ThreadLocalModule<IFilterUI,IFilter> f : Thresholder.getLoadedFilters()) {
            if(f.get().getFilterVarName().equals(filter)) {
                return f.get().exportVariables().containsKey(var);
            }
        }
        return false;
    }
    
    public RetVal getVariable(String filter, String var) {
        if(filter == null) {   // active filter
            return new RetVal(Thresholder.getActiveFilter().exportVariables().get(var));
        } else {    // the other ones
            for(ThreadLocalModule<IFilterUI,IFilter> f : Thresholder.getLoadedFilters()) {
                if(f.get().getFilterVarName().equals(filter)) {
                    return new RetVal(f.get().exportVariables().get(var));
                }
            }
        }
        return new RetVal((FloatProcessor)null);
    }
    
    public abstract RetVal eval();
    public abstract void semanticScan() throws ThresholdFormulaException;

}
