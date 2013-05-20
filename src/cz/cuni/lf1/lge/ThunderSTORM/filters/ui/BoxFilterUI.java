package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class BoxFilterUI implements IFilterUI {

  private JTextField sizeTextField;
  private int size;

  @Override
  public String getName() {
    return "Box (mean) filter";

  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel();
    sizeTextField = new JTextField(Integer.toString(size), 20);
    //
    panel.add(new JLabel("Size: "));
    panel.add(sizeTextField);
    return panel;
  }

  @Override
  public void readParameters() {
    size = Integer.parseInt(sizeTextField.getText());
  }

  @Override
  public IFilter getImplementation() {
    return new BoxFilter(size);
  }
}
