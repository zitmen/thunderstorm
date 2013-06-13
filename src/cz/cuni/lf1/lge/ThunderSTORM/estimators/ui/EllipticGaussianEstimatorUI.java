package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.CylindricalLensZEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI.LSQ;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class EllipticGaussianEstimatorUI extends SymmetricGaussianEstimatorUI implements ActionListener {

  JTextField calibrationFileTextField;
  JButton findCalibrationButton;

  @Override
  public String getName() {
    return "3D Cylindrical lens estimator";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel parentPanel = super.getOptionsPanel();

    parentPanel.add(new JLabel("Calibration file:"), GridBagHelper.leftCol());
    calibrationFileTextField = new JTextField();
    findCalibrationButton = new JButton("Find");
    findCalibrationButton.addActionListener(this);
    JPanel calibrationPanel = new JPanel(new BorderLayout());
    calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
    calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
    GridBagConstraints gbc = GridBagHelper.rightCol();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    parentPanel.add(calibrationPanel, gbc);

    return parentPanel;
  }

  @Override
  public void readParameters() {
    super.readParameters();
  }

  @Override
  public IEstimator getImplementation() {
    Yaml yaml = new Yaml();
    try {
      Object loaded = yaml.load(new FileReader(calibrationFileTextField.getText()));
      CylindricalLensCalibration calibration = (CylindricalLensCalibration) loaded;
      if (LSQ.equals(method)) {
        LSQFitter fitter = new LSQFitter(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())));
        return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius / 2, fitter));
      }
      if (MLE.equals(method)) {
        MLEFitter fitter = new MLEFitter(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())));
        return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius / 2, fitter));
      }
      throw new IllegalArgumentException("Unknown fitting method: " + method);
    } catch (FileNotFoundException ex) {
      throw new RuntimeException("Could not read calibration file.", ex);
    } catch (ClassCastException ex) {
      throw new RuntimeException("Could not parse calibration file.", ex);
    }
  }

  @Override
  public void recordOptions() {
    super.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
    int userAction = fileChooser.showOpenDialog(null);
    if (userAction == JFileChooser.APPROVE_OPTION) {
      calibrationFileTextField.setText(fileChooser.getSelectedFile().getPath());
    }
  }
}
