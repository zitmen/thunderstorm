package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.GaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class GaussianFilterUI implements IFilterUI {

  private int size = 11;
  private double sigma = 1.6;
  private JTextField sizeTextField, sigmaTextField;

  @Override
  public String getName() {
    return "Gaussian blur";
  }

  @Override
  public JPanel getOptionsPanel() {
    sizeTextField = new JTextField(Integer.toString(size), 20);
    sigmaTextField = new JTextField(Double.toString(sigma), 20);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Size: "), GridBagHelper.pos(0, 0));
    panel.add(sizeTextField, GridBagHelper.pos(1, 0));
    panel.add(new JLabel("Sigma: "), GridBagHelper.pos(0, 1));
    panel.add(sigmaTextField, GridBagHelper.pos(1, 1));
    return panel;
  }

  @Override
  public void readParameters() {
    size = Integer.parseInt(sizeTextField.getText());
    sigma = Double.parseDouble(sigmaTextField.getText());
  }

  @Override
  public IFilter getImplementation() {
    return new GaussianFilter(size, sigma);
  }
}
