package cz.cuni.lf1.lge.ThunderSTORM.results;

import javax.swing.RowFilter;

class ResultsFilter extends RowFilter {

  String filter;
  ResultsTableModel table;
  boolean [] results;
  
  int limit;
  
  public ResultsFilter(ResultsTableModel model, String text) throws IllegalArgumentException {
    table = model;
    filter = text;
    results = new boolean[table.getRowCount()];
    //
    try {
      // TODO: build the tree and run the filtering for all items at once in advance and store the results in the array of true/false values
    } catch(Exception ex) {
      throw new IllegalArgumentException("Integer required!");
    }
  }

  @Override
  public boolean include(Entry entry) {
    return results[((Integer)entry.getIdentifier()).intValue()];
  }

}
