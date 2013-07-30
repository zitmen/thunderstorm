package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import ij.IJ;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class ResultsFilter {

  private JPanel filterPanel;
  private JTextField filterTextField;
  private JButton applyButton;
  private JavaTableWindow table;
  private TripleStateTableModel model;

  public ResultsFilter(JavaTableWindow table, TripleStateTableModel model) {
    this.table = table;
    this.model = model;
  }

  public ResultsFilter() {
  }

  public JPanel createUIPanel() {
    filterPanel = new JPanel(new GridBagLayout());
    filterTextField = new JTextField();
    InputListener listener = new InputListener();
    filterTextField.addKeyListener(listener);
    applyButton = new JButton("Apply");
    applyButton.addActionListener(listener);
    filterPanel.add(new JLabel("Filter: ", SwingConstants.TRAILING));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    filterPanel.add(filterTextField, gbc);
    filterPanel.add(applyButton);
    return filterPanel;
  }

  protected void runFilter(String filterText) {
    if(filterText == null || "".equals(filterText)){
      return;
    }
    filterTextField.setBackground(Color.WHITE);
    GUI.closeBalloonTip();
    try {
      OperationsHistoryPanel opHistory = table.getOperationHistoryPanel();
      if(opHistory.getLastOperation() instanceof FilteringOperation){
        model.copyUndoToActual();
        opHistory.removeLastOperation();
      }else{
        model.copyActualToUndo();
      }
      model.setActualState();
      int all = model.getRowCount();
      applyToModel(model, filterText);
      opHistory.addOperation(new FilteringOperation(filterText));

      int filtered = all - model.getRowCount();
      String be = ((filtered > 1) ? "were" : "was");
      String item = ((all > 1) ? "items" : "item");
      table.setStatus(filtered + " out of " + all + " " + item + " " + be + " filtered out");
      table.showPreview();
    } catch (Exception ex) {
      IJ.handleException(ex);
      filterTextField.setBackground(new Color(255, 200, 200));
      GUI.showBalloonTip(filterTextField, ex.getMessage());
    }
  }

  static void applyToModel(ResultsTableModel model, String text) {
    boolean[] results = new boolean[model.getRowCount()];
    if (text.isEmpty()) {
      Arrays.fill(results, true);
    } else {
      Node tree = new FormulaParser(text, FormulaParser.FORMULA_RESULTS_FILTER).parse();
      tree.semanticScan();
      RetVal retval = tree.eval();
      if (!retval.isVector()) {
        throw new FormulaParserException("Semantic error: result of filtering formula must be a vector of boolean values!");
      }
      Double[] res = (Double[]) retval.get();
      for (int i = 0; i < res.length; i++) {
        results[i] = (res[i].doubleValue() != 0.0);
      }
      model.filterRows(results);
    }
  }

  public class FilteringOperation extends OperationsHistoryPanel.Operation {
    String filterText;

    public FilteringOperation(String filterText) {
      this.filterText = filterText;
    }
    
    @Override
    protected String getName() {
      return "Filtering";
    }

    @Override
    protected boolean isUndoAble() {
      return true;
    }

    @Override
    protected void clicked() {
      if(filterPanel.getParent() instanceof JTabbedPane){
        JTabbedPane tabbedPane = (JTabbedPane)filterPanel.getParent();
        tabbedPane.setSelectedComponent(filterPanel);
      }
      filterTextField.setText(filterText);
    }

    @Override
    protected void undo() {
      model.swapUndoAndActual();
      table.setStatus("Filtering: Undo.");
      table.showPreview();
    }

    @Override
    protected void redo() {
      model.swapUndoAndActual();
      table.setStatus("Filtering: Redo.");
      table.showPreview();
    }
  }

  private class InputListener extends KeyAdapter implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      runFilter(filterTextField.getText());
    }

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        runFilter(filterTextField.getText());
      }
    }
  }
}
