package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.CylindricalLensZEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
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

public class EllipticGaussianEstimatorUI extends SymmetricGaussianEstimatorUI implements ActionListener {

    CylindricalLensCalibration calibration;
    transient JButton findCalibrationButton;
    transient JTextField calibrationFileTextField;

    public EllipticGaussianEstimatorUI() {
        this.name = "PSF: Elliptical Gaussian (3D astigmatism)";
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel parentPanel = super.getOptionsPanel();

        parentPanel.add(new JLabel("Calibration file:"), GridBagHelper.leftCol());
        calibrationFileTextField = new JTextField(Prefs.get("thunderstorm.estimators.calibrationpath", ""));
        findCalibrationButton = new JButton("Browse...");
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
        calibration = loadCalibration(calibrationFileTextField.getText());

        Prefs.set("thunderstorm.estimators.calibrationpath", calibrationFileTextField.getText());
        
    }

    @Override
    public IEstimator getImplementation() {
        if(SymmetricGaussianEstimatorUI.LSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                IEstimator mfa = crowdedField.getLSQImplementation(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())), sigma, fitradius);
                return new CylindricalLensZEstimator(calibration, mfa);
            } else {
                LSQFitter fitter = new LSQFitter(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())));
                return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius, fitter));
            }
        }
        if(SymmetricGaussianEstimatorUI.MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                IEstimator mfa = crowdedField.getMLEImplementation(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())), sigma, fitradius);
                return new CylindricalLensZEstimator(calibration, mfa);
            } else {
                MLEFitter fitter = new MLEFitter(new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle())));
                return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius, fitter));
            }
        }
        throw new IllegalArgumentException("Unknown fitting method: " + method);

    }

    @Override
    public void recordOptions() {
        super.recordOptions();
        Recorder.recordOption("calibrationfile", calibrationFileTextField.getText().replace("\\", "\\\\"));
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        calibration = loadCalibration(Macro.getValue(options, "calibrationfile", ""));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
        int userAction = fileChooser.showOpenDialog(null);
        if(userAction == JFileChooser.APPROVE_OPTION) {
            calibrationFileTextField.setText(fileChooser.getSelectedFile().getPath());
        }
    }

    private CylindricalLensCalibration loadCalibration(String calibrationFilePath) {
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(new FileReader(calibrationFilePath));
            return (CylindricalLensCalibration) loaded;
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("Could not read calibration file.", ex);
        } catch(ClassCastException ex) {
            throw new RuntimeException("Could not parse calibration file.", ex);
        }
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
    }
}
