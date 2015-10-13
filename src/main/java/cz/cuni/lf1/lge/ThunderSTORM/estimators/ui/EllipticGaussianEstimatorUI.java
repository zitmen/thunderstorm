package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.CylindricalLensCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.*;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import ij.Prefs;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class EllipticGaussianEstimatorUI extends SymmetricGaussianEstimatorUI {

    private String calibrationFilePath;
    public CylindricalLensCalibration calibration;
    protected transient ParameterKey.String CALIBRATION_PATH;

    private transient DaostormCalibration daoCalibration;   // internal variable for calculation of uncertainty

    public EllipticGaussianEstimatorUI() {
        this.name = "PSF: Elliptical Gaussian (3D astigmatism)";
        CALIBRATION_PATH = parameters.createStringField("calibrationpath", StringValidatorFactory.fileExists(), "");
        daoCalibration = null;
    }

    public DaostormCalibration getDaoCalibration() {
        if (daoCalibration == null) {
            daoCalibration = calibration.getDaoCalibration();
        }
        return daoCalibration;
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
        JButton findCalibrationButton = DialogStub.createBrowseButton(calibrationFileTextField, true, new FileNameExtensionFilter("Yaml file", "yaml"));
        JPanel calibrationPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return ((JTextField) parameters.getRegisteredComponent(SIGMA)).getPreferredSize();
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
        method = METHOD.getValue();
        initialSigma = SIGMA.getValue();
        fittingRadius = FITRAD.getValue();
        fullImageFitting = FULL_IMAGE_FITTING.getValue();
        PSFModel psf = new EllipticGaussianPSF(calibration);
        OneLocationFitter fitter;
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getLSQImplementation(psf, initialSigma);
            } else {
                fitter = new LSQFitter(psf, WLSQ.equals(method), Params.BACKGROUND);
            }

        } else if(MLE.equals(method)) {
            if(crowdedField.isEnabled()) {
                fitter = crowdedField.getMLEImplementation(psf, initialSigma);
            } else {
                fitter = new MLEFitter(psf, Params.BACKGROUND);
            }
        } else {
            throw new IllegalArgumentException("Unknown fitting method: " + method);
        }
        IEstimator estimator = fullImageFitting
                ? new FullImageFitting(fitter)
                : new MultipleLocationsImageFitting(fittingRadius, fitter);
        return new CylindricalLensZEstimator(estimator);

    }

    private CylindricalLensCalibration loadCalibration(String calibrationFilePath) {
        this.calibrationFilePath = calibrationFilePath;
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
