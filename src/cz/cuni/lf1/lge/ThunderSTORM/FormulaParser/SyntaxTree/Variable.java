package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;

public class Variable extends Node {
    
  private String obj = null;
  private String var = null;

  @Override
  public void semanticScan() throws FormulaParserException {
    if(obj == null) {   // active filter
      if(isThresholding()) {
        if(!isVariable(Thresholder.getActiveFilter().getFilterVarName(), var)) {
          throw new FormulaParserException("Variable '" + var + "' does not exist!");
        }
      } else {
        if(!isVariable(null, var)) {
          throw new FormulaParserException("Variable '" + var + "' does not exist!");
        }
      }
    } else {    // the other ones
      if(!isVariable(obj, var)) {
        throw new FormulaParserException("Variable '" + obj + "." + var + "' does not exist!");
      }
    }
  }

  public Variable(String objName, String varName) {
    obj = objName;
    var = varName;
  }

  public Variable(String varName) {
    var = varName;
  }

  @Override
  public RetVal eval() {
    RetVal retval = getVariable(obj, var);
    assert(retval != null); // semantic check ensures this will never happen
    return retval;
  }

}
