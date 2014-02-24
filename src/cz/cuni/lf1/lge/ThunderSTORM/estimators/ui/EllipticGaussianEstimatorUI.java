package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.CylindricalLensZEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.FullImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.StringValidatorFactory;
import ij.IJ;
import ij.Prefs;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
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

public class EllipticGaussianEstimatorUI extends SymmetricGaussianEstimatorUI {

    CylindricalLensCalibration calibration;
    protected transient ParameterKey.String CALIBRATION_PATH;

    public EllipticGaussianEstimatorUI() {
        this.name = "PSF: Elliptical Gaussian (3D astigmatism)";
        CALIBRATION_PATH = parameters.createStringField("calibrationpath", StringValidatorFactory.fileExists(), "");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel parentPanel = super.getOptionsPanel();

        parentPanel.add(new JLabel("Calibration file:"), GridBagHelper.leftCol());
        final JTextField calibrationFileTextField = new JTextField(Prefs.get("thunderstorm.estimators.calibrationpath", ""));
        parameters.registerComponent(CALIBRATION_PATH, calibrationFileTextField);
        JButton findCalibrationButton = new JButton("...");
        findCalibrationButton.setMargin(new Insets(1, 1, 1, 1));
        findCalibrationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
                int userAction = fileChooser.showOpenDialog(null);
                if(userAction == JFileChooser.APPROVE_OPTION) {
                    calibrationFileTextField.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        JPanel calibrationPanel = new JPanel(new BorderLayout()){
            @Override
            public Dimension getPreferredSize(){
                return ((JTextField)parameters.getRegisteredComponent(SIGMA)).getPreferredSize();
            }
        };
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        parentPanel.add(calibrationPanel, gbc);

        parameters.loadPrefs();
        return parentPanel;
    }

    @Override
    public void readParameters() {
        super.readParameters();
        calibration = loadCalibration(parameters.getString(CALIBRATION_PATH));
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        calibration = loadCalibration(parameters.getString(CALIBRATION_PATH));
    }

    @Override
    public IEstimator getImplementation() {
        String method = METHOD.getValue();
        double sigma = SIGMA.getValue();
        int fitradius = FITRAD.getValue();
        EllipticGaussianPSF psf = new EllipticGaussianPSF(sigma, Math.toRadians(calibration.getAngle()));
        if(FULL_IMAGE_FITTING.getValue() == true) {
            FullImageFitting fif = new FullImageFitting(psf, method, crowdedField);
            return new CylindricalLensZEstimator(calibration, fif);
        } else {
            if(LSQ.equals(method) || WLSQ.equals(method)) {
                if(crowdedField.isEnabled()) {
                    IEstimator mfa = crowdedField.getLSQImplementation(psf, sigma, fitradius);
                    return new CylindricalLensZEstimator(calibration, mfa);
                } else {
                    LSQFitter fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
                    return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius, fitter));
                }
            }
            if(MLE.equals(method)) {
                if(crowdedField.isEnabled()) {
                    IEstimator mfa = crowdedField.getMLEImplementation(psf, sigma, fitradius);
                    return new CylindricalLensZEstimator(calibration, mfa);
                } else {
                    MLEFitter fitter = new MLEFitter(psf, Params.BACKGROUND);
                    return new CylindricalLensZEstimator(calibration, new MultipleLocationsImageFitting(fitradius, fitter));
                }
            }
        }
        throw new IllegalArgumentException("Unknown fitting method: " + method);

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
}
