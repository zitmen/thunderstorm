package cz.cuni.lf1.lge.ThunderSTORM.detectors.ui;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CentroidOfConnectedComponentsDetectorUI implements IDetectorUI {

  private boolean upsample;
  private String threshold = "std(I-Wave.V1)";
  private JTextField thrTextField;
  private JCheckBox upCheckBox;

  @Override
  public String getName() {
    return "Centroid of connected components";
  }

  @Override
  public JPanel getOptionsPanel() {
    thrTextField = new JTextField(threshold.toString(), 20);
    upCheckBox = new JCheckBox("upsample");
    upCheckBox.setSelected(upsample);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
    panel.add(thrTextField, GridBagHelper.pos(1, 0));
    panel.add(upCheckBox, GridBagHelper.pos_size(0, 1, 2, 1));
    return panel;
  }

  @Override
  public void readParameters() {
    threshold = thrTextField.getText();
    upsample = upCheckBox.isSelected();
  }

  @Override
  public IDetector getImplementation() {
    try {
      return new CentroidOfConnectedComponentsDetector(upsample, threshold);
    } catch (ThresholdFormulaException ex) {
      throw new RuntimeException(ex);
    }
  }
}
