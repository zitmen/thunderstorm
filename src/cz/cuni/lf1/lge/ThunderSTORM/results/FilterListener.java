package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;
import net.java.balloontip.BalloonTip;

class FilterListener implements ActionListener, KeyListener {

  private JLabel status;
  private JTextField filter;
  private ResultsTableModel model;
  private TableRowSorter<ResultsTableModel> sorter;
  
  public FilterListener(ResultsTableModel model, TableRowSorter<ResultsTableModel> sorter, JTextField filter, JLabel status) {
    this.model = model;
    this.sorter = sorter;
    this.filter = filter;
    this.status = status;
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
      ResultsFilter rf = new ResultsFilter(model, filter.getText());
      sorter.setRowFilter(rf);
      int filtered = rf.getFilteredItemsCount(), all = rf.getAllItemsCount();
      String be = ((filtered > 1) ? "were" : "was");
      String item = ((all > 1) ? "items" : "item");
      status.setText(filtered + " out of " + all + " " + item + " " + be + " filtered out");
      filter.setBackground(Color.WHITE);
    } catch(Exception ex) {
      filter.setBackground(new Color(255, 200, 200));
      new BalloonTip(filter, ex.getMessage());
    }
  }

}
