package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LeastSquaresEstimator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class LeastSquaresEstimatorUI implements IEstimatorUI {

  private JTextField fitregsizeTextField;
  private int radius;

  @Override
  public String getName() {
    return "Minimizing least squares error";
  }

  @Override
  public JPanel getOptionsPanel() {
    fitregsizeTextField = new JTextField(Integer.toString(radius), 20);
    //
    JPanel panel = new JPanel();
    panel.add(new JLabel("Fitting region size: "));
    panel.add(fitregsizeTextField);
    return panel;
  }

  @Override
  public void readParameters() {
    radius = Integer.parseInt(fitregsizeTextField.getText());
  }

  @Override
  public IEstimator getImplementation() {
    return new LeastSquaresEstimator(radius);
  }
}
