package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.plugin.frame.Recorder;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class EllipticGaussianEstimatorUI extends SymmetricGaussianEstimatorUI {

  double angle;
  JTextField angleTextField;

  @Override
  public String getName() {
    return "Cylindrical lens estimator";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel parentPanel = super.getOptionsPanel();

    angleTextField = new JTextField("0");
    parentPanel.add(new JLabel("Angle [radians]:"), GridBagHelper.leftCol());
    parentPanel.add(angleTextField, GridBagHelper.rightCol());
    return parentPanel;
  }

  @Override
  public void readParameters() {
    super.readParameters();
    angle = Double.parseDouble(angleTextField.getText());
  }

  @Override
  public IEstimator getImplementation() {
    if (LSQ.equals(method)) {
      LSQFitter fitter = new LSQFitter(new EllipticGaussianPSF(sigma, angle));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    if (MLE.equals(method)) {
      MLEFitter fitter = new MLEFitter(new EllipticGaussianPSF(sigma, angle));
      return new MultipleLocationsImageFitting(fitradius / 2, fitter);
    }
    throw new IllegalArgumentException("Unknown fitting method: " + method);
  }

  @Override
  public void recordOptions() {
    super.recordOptions();
    if (angle != 0) {
      Recorder.recordOption("angle", Double.toString(angle));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
    angle = Double.parseDouble(Macro.getValue(options, "angle", "0"));
  }
}
