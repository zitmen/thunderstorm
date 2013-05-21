package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.LocalMaximaDetector;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class LocalMaximaDetectorUI implements IDetectorUI {

  private int connectivity = Graph.CONNECTIVITY_8;
  private String threshold = "10*std(F)";
  private JTextField thrTextField;
  private JRadioButton conn4RadioButton, conn8RadioButton;

  @Override
  public String getName() {
    return "Search for local maxima";
  }

  @Override
  public JPanel getOptionsPanel() {
    thrTextField = new JTextField(threshold.toString(), 20);
    conn4RadioButton = new JRadioButton("4-neighbourhood");
    conn8RadioButton = new JRadioButton("8-neighbourhood");
    //
    conn4RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_4);
    conn8RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_8);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
    panel.add(thrTextField, GridBagHelper.pos(1, 0));
    panel.add(new JLabel("Connectivity: "), GridBagHelper.pos(0, 1));
    panel.add(conn8RadioButton, GridBagHelper.pos(1, 1));
    panel.add(conn4RadioButton, GridBagHelper.pos(1, 2));
    return panel;
  }

  @Override
  public void readParameters() {
    threshold = thrTextField.getText();
    if (conn4RadioButton.isSelected()) {
      connectivity = Graph.CONNECTIVITY_4;
    }
    if (conn8RadioButton.isSelected()) {
      connectivity = Graph.CONNECTIVITY_8;
    }
  }

  @Override
  public IDetector getImplementation() {
    try {
      return new LocalMaximaDetector(connectivity, threshold);
    } catch (ThresholdFormulaException ex) {
      throw new RuntimeException(ex);
    }
  }
}
