package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.RadialSymmetryFitter;
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
public class RadialSymmetryEstimatorUI implements IEstimatorUI {

  protected int fitradius;
  protected JTextField fitregsizeTextField;
  private static final int DEFAULT_FITRAD = 5;

  @Override
  public String getName() {
    return "Radial symmetry estimator";
  }

  @Override
  public JPanel getOptionsPanel() {
    fitregsizeTextField = new JTextField(Integer.toString(DEFAULT_FITRAD), 20);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Region size:"), GridBagHelper.leftCol());
    panel.add(fitregsizeTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    fitradius = Integer.parseInt(fitregsizeTextField.getText());
  }

  @Override
  public void recordOptions() {
    if (fitradius != DEFAULT_FITRAD) {
      Recorder.recordOption("fitrad", Integer.toString(fitradius));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    fitradius = Integer.parseInt(Macro.getValue(options, "fitrad", Integer.toString(DEFAULT_FITRAD)));
  }

  @Override
  public IEstimator getImplementation() {
    return new MultipleLocationsImageFitting(fitradius, new RadialSymmetryFitter());
  }
}
