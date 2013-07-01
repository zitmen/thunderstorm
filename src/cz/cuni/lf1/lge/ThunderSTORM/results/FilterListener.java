package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;

class FilterListener implements ActionListener, KeyListener {

  private JTextField filter;
  private TableRowSorter<ResultsTableModel> sorter;
  
  public FilterListener(TableRowSorter<ResultsTableModel> sorter, JTextField filter) {
    this.sorter = sorter;
    this.filter = filter;
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    runFilter();
  }
  
  @Override
  public void keyTyped(KeyEvent e) {
    // nothing to do
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
      runFilter();
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    // nothing to do
  }
  
  //
  // =====================================================================
  //
  
  protected void runFilter() {
    try {
      sorter.setRowFilter(new ResultsFilter(filter.getText()));
    } catch(IllegalArgumentException ex) {
      filter.setBackground(new Color(255, 200, 200));
      filter.setToolTipText("Wrong syntax! " + ex.getMessage());
    }
  }

}
