package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.NonMaxSuppressionDetector;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class NonMaxSuppressionDetectorUI implements IDetectorUI {

  private int radius = 3;
  private String threshold = "6*std(F)";
  private JTextField thrTextField;
  private JTextField radiusTextField;

  @Override
  public String getName() {
    return "Non-maxima suppression";
  }

  @Override
  public JPanel getOptionsPanel() {
    thrTextField = new JTextField(threshold.toString(), 20);
    radiusTextField = new JTextField(Integer.toString(radius), 20);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
    panel.add(thrTextField, GridBagHelper.pos(1, 0));
    panel.add(new JLabel("Radius: "), GridBagHelper.pos(0, 1));
    panel.add(radiusTextField, GridBagHelper.pos(1, 1));
    return panel;
  }

  @Override
  public void readParameters() {
    threshold = thrTextField.getText();
    radius = Integer.parseInt(radiusTextField.getText());
  }

  @Override
  public IDetector getImplementation() {
    try {
      return new NonMaxSuppressionDetector(radius, threshold);
    } catch (ThresholdFormulaException ex) {
      throw new RuntimeException(ex);
    }
  }
}
