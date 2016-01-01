package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.*;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.*;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.BiplaneEllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import ij.Prefs;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class EllipticBiplaneGaussianEstimatorUI extends IBiplaneEstimatorUI {

    public transient static final String MLE = "Maximum likelihood";
    public transient static final String LSQ = "Least squares";
    public transient static final String WLSQ = "Weighted Least squares";
    //
    protected String name = "PSF: Elliptical Gaussian";
    protected int fittingRadius;
    protected String method;
    protected boolean numericalDerivatives;
    protected double distThrPx;
    protected DoubleDefocusCalibration<DaostormCalibration> calibration;
    protected String calibrationFilePath;
    //params
    protected transient ParameterKey.Integer FITRAD;
    protected transient ParameterKey.String METHOD;
    protected transient ParameterKey.Boolean NUMERICAL_DERIVATIVES;
    protected transient ParameterKey.Double MATCHING_DISTANCE_THRESHOLD;
    protected transient ParameterKey.String CALIBRATION_PATH;

    public EllipticBiplaneGaussianEstimatorUI() {
        FITRAD = parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 3);
        METHOD = parameters.createStringField("method", StringValidatorFactory.isMember(new String[]{MLE, LSQ, WLSQ}), MLE);
        NUMERICAL_DERIVATIVES = parameters.createBooleanField("numericalDerivatives", null, false);
        CALIBRATION_PATH = parameters.createStringField("calibrationpath", StringValidatorFactory.fileExists(), "");
        // it's better to make the following distance larger and let the matcher do it's job; otherwise,
        // a localization uncertainty would be the way to go, however, it would require an additional round of fitting
        MATCHING_DISTANCE_THRESHOLD = parameters.createDoubleField("distThrPx", DoubleValidatorFactory.positive(), 3);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public boolean useNumericalDerivatives() {
        return numericalDerivatives;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fitregsizeTextField = new JTextField("", 20);
        JComboBox<String> methodComboBox = new JComboBox<String>(new String[]{LSQ, WLSQ, MLE});
        JCheckBox numericalDerivativesCheckBox = new JCheckBox("use numerical derivatives");
        JTextField distThrPxField = new JTextField("");
        final JTextField calibrationFileTextField = new JTextField(Prefs.get("thunderstorm.estimators.calibrationpath", ""));
        parameters.registerComponent(FITRAD, fitregsizeTextField);
        parameters.registerComponent(METHOD, methodComboBox);
        parameters.registerComponent(NUMERICAL_DERIVATIVES, numericalDerivativesCheckBox);
        parameters.registerComponent(MATCHING_DISTANCE_THRESHOLD, distThrPxField);
        parameters.registerComponent(CALIBRATION_PATH, calibrationFileTextField);

        JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel("Calibration file:"), GridBagHelper.leftCol());
        JButton findCalibrationButton = DialogStub.createBrowseButton(calibrationFileTextField, true, new FileNameExtensionFilter("Yaml file", "yaml"));
        JPanel calibrationPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return ((JTextField) parameters.getRegisteredComponent(FITRAD)).getPreferredSize();
            }
        };
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(calibrationPanel, gbc);

        panel.add(new JLabel("Maximum biplane matching distance [px]:"), GridBagHelper.leftCol());
        panel.add(distThrPxField, GridBagHelper.rightCol());
        panel.add(new JLabel("Fitting radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Fitting method:"), GridBagHelper.leftCol());
        panel.add(methodComboBox, GridBagHelper.rightCol());
        panel.add(numericalDerivativesCheckBox, GridBagHelper.rightCol());

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public void readParameters() {
        super.readParameters();
        calibrationFilePath = parameters.getString(CALIBRATION_PATH);
        calibration = loadCalibration(calibrationFilePath);
    }

    @Override
    public IBiplaneEstimator getImplementation() {
        method = METHOD.getValue();
        numericalDerivatives = NUMERICAL_DERIVATIVES.getValue();
        distThrPx = MATCHING_DISTANCE_THRESHOLD.getValue();
        fittingRadius = FITRAD.getValue();
        PSFModel psf = new BiplaneEllipticGaussianPSF(calibration, numericalDerivatives);
        IOneLocationBiplaneFitter fitter;
        if(LSQ.equals(method) || WLSQ.equals(method)) {
            fitter = new LSQFitter(psf, WLSQ.equals(method), PSFModel.Params.BACKGROUND);
        } else if(MLE.equals(method)) {
            fitter = new MLEFitter(psf, PSFModel.Params.BACKGROUND);
        } else {
            throw new IllegalArgumentException("Unknown fitting method: " + method);
        }
        return new MultipleLocationsBiplaneFitting(fittingRadius, distThrPx, calibration.homography, fitter);
    }

    @Override
    public void recordOptions() {
        super.recordOptions();
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        calibrationFilePath = parameters.getString(CALIBRATION_PATH);
        calibration = loadCalibration(calibrationFilePath);
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
    }

    private static DoubleDefocusCalibration<DaostormCalibration> loadCalibration(String calibrationFilePath) {
        FileReader fr = null;
        Homography.TransformationMatrix homography = null;
        DefocusCalibration cal1 = null;
        DefocusCalibration cal2 = null;
        try {
            fr = new FileReader(calibrationFilePath);
            for (Object obj : new Yaml(new Homography.TransformationMatrix.YamlConstructor()).loadAll(fr)) {
                if (obj instanceof Homography.TransformationMatrix) {
                    homography = (Homography.TransformationMatrix) obj;
                } else if (obj instanceof DefocusCalibration) {
                    if (cal1 == null) {
                        cal1 = (DefocusCalibration) obj;
                    } else if (cal2 == null) {
                        cal2 = (DefocusCalibration) obj;
                    } else {
                        throw new RuntimeException("Too many calibration classes for biplane analysis!");
                    }
                } else {
                    throw new RuntimeException("Unknown calibration class incompatible with biplane analysis!");
                }
            }
            if (homography == null) {
                throw new RuntimeException("Transformation matrix is missing from the calibration data!");
            }
            if (cal1 == null) {
                throw new RuntimeException("Defocusing curve estimate is missing from the calibration data!");
            }
            if (cal2 == null) {
                throw new RuntimeException("Biplane calibration requires two defocusing curves, but only one was found!");
            }
            cal1.homography = homography;
            cal2.homography = homography;
            return new DoubleDefocusCalibration<DaostormCalibration>(DefocusFunctionSqrt.name,
                    homography, cal1.getDaoCalibration(), cal2.getDaoCalibration());
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("Could not read calibration file.", ex);
        } catch(ClassCastException ex) {
            throw new RuntimeException("Could not parse calibration file.", ex);
        } finally {
            if(fr != null) {
                try { fr.close(); } catch (IOException ignored) { }
            }
        }
    }
}
