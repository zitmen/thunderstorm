package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
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
public class DifferenceOfGaussiansFilterUI implements IFilterUI {

  private JTextField sigma1TextField, sigma2TextField, sizeTextField;
  private double sigma_g1, sigma_g2;
  private int size;

  @Override
  public String getName() {
    return "Difference of Gaussians";
  }

  @Override
  public JPanel getOptionsPanel() {
    sizeTextField = new JTextField(Integer.toString(size), 20);
    sigma1TextField = new JTextField(Double.toString(sigma_g1), 20);
    sigma2TextField = new JTextField(Double.toString(sigma_g2), 20);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Size: "), GridBagHelper.pos(0, 0));
    panel.add(sizeTextField, GridBagHelper.pos(1, 0));
    panel.add(new JLabel("Sigma1: "), GridBagHelper.pos(0, 1));
    panel.add(sigma1TextField, GridBagHelper.pos(1, 1));
    panel.add(new JLabel("Sigma2: "), GridBagHelper.pos(0, 2));
    panel.add(sigma2TextField, GridBagHelper.pos(1, 2));
    return panel;
  }

  @Override
  public void readParameters() {
    size = Integer.parseInt(sizeTextField.getText());
    sigma_g1 = Double.parseDouble(sigma1TextField.getText());
    sigma_g2 = Double.parseDouble(sigma2TextField.getText());
  }

  @Override
  public IFilter getImplementation() {
    return new DifferenceOfGaussiansFilter(size, sigma_g1, sigma_g2);
  }
}
