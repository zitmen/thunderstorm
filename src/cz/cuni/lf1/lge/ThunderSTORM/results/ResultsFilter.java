package cz.cuni.lf1.lge.ThunderSTORM.results;

import javax.swing.RowFilter;

class ResultsFilter extends RowFilter {

  int limit;
  
  public ResultsFilter(String text) throws IllegalArgumentException {
    try {
      limit = Integer.parseInt(text);
    } catch(NumberFormatException ex) {
      throw new IllegalArgumentException("Integer required!");
    }
  }

  @Override
  public boolean include(Entry entry) {
    ResultsTableModel model = (ResultsTableModel)entry.getModel();
    int row = ((Integer)entry.getIdentifier()).intValue();
    //for(int col = 0, colm = entry.getValueCount(); col < colm; col++) {
      //
    //}
    return (((Double)model.getValueAt(row, 0)).intValue() <= limit);
  }

}
