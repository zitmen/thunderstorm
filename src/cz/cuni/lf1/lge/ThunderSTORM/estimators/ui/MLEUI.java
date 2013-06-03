package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import ij.Macro;
import ij.plugin.frame.Recorder;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class MLEUI implements IEstimatorUI {

  private int fitradius;
  private JTextField fitregsizeTextField;
  private static final int DEFAULT_FITRAD = 5;

  @Override
  public String getName() {
    return "MLE plugin";
  }

  @Override
  public JPanel getOptionsPanel() {
    fitregsizeTextField = new JTextField(Integer.toString(DEFAULT_FITRAD), 20);
    //
    JPanel panel = new JPanel();
    panel.add(new JLabel("Fitting region size: "));
    panel.add(fitregsizeTextField);
    return panel;
  }

  @Override
  public void readParameters() {
    fitradius = Integer.parseInt(fitregsizeTextField.getText());
  }

  @Override
  public IEstimator getImplementation() {
    return new MultipleLocationsImageFitting(fitradius / 2, new MLEFitter(new SymmetricGaussianPSF(1.6)));
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
}
