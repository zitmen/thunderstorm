package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;

public abstract class Node {
    
    public boolean isVariable(String filter, String var) {
        for(IFilter f : Thresholder.getLoadedFilters()) {
            if(f.getFilterVarName().equals(filter)) {
                return f.exportVariables().containsKey(var);
            }
        }
        return false;
    }
    
    public RetVal getVariable(String filter, String var) {
        if(filter == null) {   // active filter
            return new RetVal(Thresholder.getActiveFilter().exportVariables().get(var));
        } else {    // the other ones
            for(IFilter f : Thresholder.getLoadedFilters()) {
                if(f.getFilterVarName().equals(filter)) {
                    return new RetVal(f.exportVariables().get(var));
                }
            }
        }
        return new RetVal((FloatProcessor)null);
    }
    
    public abstract RetVal eval();
    public abstract void semanticScan() throws ThresholdFormulaException;

}
