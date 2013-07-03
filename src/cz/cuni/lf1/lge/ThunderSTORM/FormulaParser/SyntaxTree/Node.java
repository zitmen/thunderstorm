package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;

public abstract class Node {
  
    public static final int THRESHOLDING = 1;
    public static final int RESULTS_FILTERING = 2;
    
    private int nodeType;
    
    public void setNodeType(int action) {
      this.nodeType = action;
    }
    
    public int getNodeType() {
      return nodeType;
    }
    
    public boolean isThresholding() {
      return (nodeType == THRESHOLDING);
    }
    
    public boolean isResultsFiltering() {
      return (nodeType == RESULTS_FILTERING);
    }
    
    public boolean isVariable(String filter, String var) {
        for(IFilterUI f : Thresholder.getLoadedFilters()) {
          IFilter impl = f.getImplementation();
            if(impl.getFilterVarName().equals(filter)) {
                return impl.exportVariables(false).containsKey(var);
            }
        }
        return false;
    }
    
    public RetVal getVariable(String filter, String var) {
        if(filter == null) {   // active filter
            // this filter is already active, hence there is no need to redo the filtering step,
            // since it has been already done
            return new RetVal(Thresholder.getActiveFilter().exportVariables(false).get(var));
        } else {    // the other ones
            for(IFilterUI f : Thresholder.getLoadedFilters()) {
              IFilter impl = f.getImplementation();
                if(impl.getFilterVarName().equals(filter)) {
                    return new RetVal(impl.exportVariables(true).get(var));
                }
            }
        }
        return new RetVal((FloatProcessor)null);
    }
    
    public abstract RetVal eval();
    public abstract void semanticScan() throws FormulaParserException;

}
