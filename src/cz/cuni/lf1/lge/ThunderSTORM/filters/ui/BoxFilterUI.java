package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
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
  private static final int DEFAULT_SIZE = 3;

  @Override
  public String getName() {
    return "Box (mean) filter";

  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    sizeTextField = new JTextField(Integer.toString(DEFAULT_SIZE), 20);
    //
    panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
    panel.add(sizeTextField, GridBagHelper.rightCol());
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

  @Override
  public void recordOptions() {
    if (size != DEFAULT_SIZE) {
      Recorder.recordOption("size", Integer.toString(size));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    size = Integer.parseInt(Macro.getValue(options, "size", Integer.toString(DEFAULT_SIZE)));
  }
}
