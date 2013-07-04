package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;
import java.util.Arrays;
import javax.swing.RowFilter;

class ResultsFilter extends RowFilter {

  private Node tree;
  private String filter;
  private ResultsTableModel table;
  private boolean [] results;
  
  public ResultsFilter(ResultsTableModel model, String text) {
    table = model;
    filter = text;
    results = new boolean[table.getRowCount()];
    //
    if(text.isEmpty()) {    // no filter
        Arrays.fill(results, true);
    } else {
        tree = new FormulaParser(filter, FormulaParser.FORMULA_RESULTS_FILTER).parse();
        tree.semanticScan();
        RetVal retval = tree.eval();
        if(!retval.isVector()) {
            throw new FormulaParserException("Semantic error: result of filtering formula must be a vector of boolean values!");
        }
        Double [] res = (Double[])retval.get();
        for(int i = 0; i < res.length; i++) {
          results[i] = (res[i].doubleValue() != 0.0);
        }
    }
  }

  @Override
  public boolean include(Entry entry) {
    return results[((Integer)entry.getIdentifier()).intValue()];
  }
  
  public int getFilteredItemsCount() {
      int count = 0;
      for(int i = 0; i < results.length; i++) {
          if(results[i] == false) {
              count++;
          }
      }
      return count;
  }
  
  public int getAllItemsCount() {
      return results.length;
  }

}
