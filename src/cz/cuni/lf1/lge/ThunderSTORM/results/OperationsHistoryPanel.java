package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OperationsHistoryPanel extends JPanel {

  private List<LabelWithCheckbox> stack;
  private JLabel historyLabel;
  private final static String LABEL = "Post-processing history: ";
  private final static String LABEL_EMPTY = "Post-processing history: -";

  public OperationsHistoryPanel() {
    stack = new ArrayList<LabelWithCheckbox>();
    this.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
    historyLabel = new JLabel(OperationsHistoryPanel.LABEL_EMPTY);
    add(historyLabel);
  }

  public void addOperation(Operation op) {
    if(!stack.isEmpty() && !stack.get(stack.size() - 1).isChecked()){
      removeLastOperation();
    }
    //add arrow
    JLabel arrow = null;
    if (!stack.isEmpty()) {
      arrow = new JLabel("\u2192");
      add(arrow);
    } else {
        historyLabel.setText(OperationsHistoryPanel.LABEL);
    }

    LabelWithCheckbox opLabel = new LabelWithCheckbox(op, arrow);
    add(opLabel);
    stack.add(opLabel);
    disableNextToLastCheckbox();
    revalidate();
  }

  public Operation getLastOperation() {
    return stack.isEmpty() ? null : stack.get(stack.size() - 1).getOperation();
  }

  public void removeAllOperations() {
    stack.clear();
    removeAll();
    historyLabel.setText(OperationsHistoryPanel.LABEL_EMPTY);
    add(historyLabel);
    repaint();
  }

  public Operation removeLastOperation() {
    int opCount = stack.size();
    if (opCount > 0) {
      LabelWithCheckbox last = stack.remove(opCount - 1);
      if (last.arrow != null) {
        remove(last.arrow);   //remove preceding arrow
      }
      remove(last);
      revalidate();
      return last.getOperation();
    }
    return null;
  }

  public void disableNextToLastCheckbox() {
    int opCount = stack.size();
    if (opCount > 1) {
      LabelWithCheckbox nextToLast = stack.get(opCount - 2);
      nextToLast.removeCheckbox();
    }
  }

  private class LabelWithCheckbox extends JPanel {

    JCheckBox chb;
    JLabel lab;
    JLabel arrow;
    Operation op;

    public LabelWithCheckbox(final Operation op, JLabel arrow) {
      this.op = op;
      this.arrow = arrow;
      setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
      setBorder(javax.swing.BorderFactory.createEtchedBorder());
      //label
      lab = new JLabel(op.getName());
      lab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lab.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          op.clicked();
        }
      });
      add(lab);
      //checkbox
      if (op.isUndoAble()) {
        chb = new JCheckBox();
        chb.setBorder(null);
        chb.setSelected(true);
        chb.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (chb.isSelected()) {
              op.redo();
            } else {
              op.undo();
            }
          }
        });
        add(chb);
      }
    }

    public void removeCheckbox() {
      if (chb != null) {
        remove(chb);
        chb = null;
        revalidate();
      }
    }

    public boolean isChecked() {
      return chb == null ? true : chb.isSelected();
    }

    public Operation getOperation() {
      return op;
    }
  }

  public static abstract class Operation {

    protected abstract String getName();

    protected boolean isUndoAble() {
      return false;
    }

    protected void clicked() {
    }

    protected void undo() {
    }

    protected void redo() {
    }
  }
}
