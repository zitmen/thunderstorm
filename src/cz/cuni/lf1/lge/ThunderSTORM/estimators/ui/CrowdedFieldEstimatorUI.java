package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.Validator;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.ValidatorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CrowdedFieldEstimatorUI {

    ParameterTracker params;
    private final String name = "Multi-emitter fitting analysis";
    //default values
    protected transient boolean DEFAULT_ENABLED = false;
    protected transient int DEFAULT_NMAX = 5;
    protected transient double DEFAULT_PVALUE = 1e-6;
    protected transient boolean DEFAULT_FIXED_INTENSITY = false;
    protected transient boolean DEFAULT_KEEP_SAME_INTENSITY = true;
    protected transient String DEFAULT_INTENSITY_RANGE = "500:2500";
    //parameter names
    protected transient static final ParameterName.Boolean ENABLED = new ParameterName.Boolean("mfaenabled");
    protected transient static final ParameterName.Integer NMAX = new ParameterName.Integer("nmax");
    protected transient static final ParameterName.Double PVALUE = new ParameterName.Double("pvalue");
    protected transient static final ParameterName.Boolean KEEP_SAME_INTENSITY = new ParameterName.Boolean("keep_same_intensity");
    protected transient static final ParameterName.Boolean FIXED_INTENSITY = new ParameterName.Boolean("fixed_intensity");
    protected transient static final ParameterName.String INTENSITY_RANGE = new ParameterName.String("expected_intensity");

    public CrowdedFieldEstimatorUI() {
        params = new ParameterTracker("thunderstorm.estimators.dense.mfa");
        ParameterTracker.Condition enabledCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return params.getBoolean(ENABLED);
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{ENABLED};
            }
        };
        ParameterTracker.Condition enabledAndFixedIntensityCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return params.getBoolean(ENABLED) && params.getBoolean(FIXED_INTENSITY);
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{ENABLED, FIXED_INTENSITY};
            }
        };
        params.createBooleanField(ENABLED, null, DEFAULT_ENABLED);
        params.createIntField(NMAX, IntegerValidatorFactory.positive(), DEFAULT_NMAX, enabledCondition);
        params.createDoubleField(PVALUE, DoubleValidatorFactory.positive(), DEFAULT_PVALUE, enabledCondition);
        params.createBooleanField(KEEP_SAME_INTENSITY, null, DEFAULT_KEEP_SAME_INTENSITY, enabledCondition);
        params.createBooleanField(FIXED_INTENSITY, null, DEFAULT_FIXED_INTENSITY, enabledCondition);
        params.createStringField(INTENSITY_RANGE, new Validator<String>() {
            @Override
            public void validate(String input) throws ValidatorException {
                try {
                    Range r = Range.parseFromTo(input);
                } catch(RuntimeException ex) {
                    throw new ValidatorException(ex);
                }
            }
        }, DEFAULT_INTENSITY_RANGE, enabledAndFixedIntensityCondition);
    }

    public boolean isEnabled() {
        return params.getBoolean(ENABLED);
    }

    public JPanel getOptionsPanel(JPanel panel) {
        final JCheckBox isEnabledCheckbox = new JCheckBox("enable", true);
        final JTextField nMaxTextField = new JTextField("");
        final JTextField pValueTextField = new JTextField("");
        final JCheckBox keepSameIntensityCheckBox = new JCheckBox("Keep the same intensity of all molecules",false);
        final JCheckBox isFixedIntensityCheckBox = new JCheckBox("Fix intensity to the range [photons]:", true);
        final JTextField expectedIntensityTextField = new JTextField("");
        isEnabledCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nMaxTextField.setEnabled(isEnabledCheckbox.isSelected());
                pValueTextField.setEnabled(isEnabledCheckbox.isSelected());
                keepSameIntensityCheckBox.setEnabled(isEnabledCheckbox.isSelected());
                isFixedIntensityCheckBox.setEnabled(isEnabledCheckbox.isSelected());
                expectedIntensityTextField.setEnabled(isEnabledCheckbox.isSelected() && isFixedIntensityCheckBox.isSelected());
            }
        });
        isFixedIntensityCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expectedIntensityTextField.setEnabled(isEnabledCheckbox.isSelected() && isFixedIntensityCheckBox.isSelected());
            }
        });
        params.registerComponent(ENABLED, isEnabledCheckbox);
        params.registerComponent(NMAX, nMaxTextField);
        params.registerComponent(PVALUE, pValueTextField);
        params.registerComponent(KEEP_SAME_INTENSITY, keepSameIntensityCheckBox);
        params.registerComponent(FIXED_INTENSITY, isFixedIntensityCheckBox);
        params.registerComponent(INTENSITY_RANGE, expectedIntensityTextField);

        panel.add(new JLabel(name + ":"), GridBagHelper.leftCol());
        panel.add(isEnabledCheckbox, GridBagHelper.rightCol());
        panel.add(new JLabel("Maximum of molecules per fitting region:"), GridBagHelper.leftCol());
        panel.add(nMaxTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Threshold for more complicated model (p-value):"), GridBagHelper.leftCol());
        panel.add(pValueTextField, GridBagHelper.rightCol());
        panel.add(keepSameIntensityCheckBox, GridBagHelper.leftCol());
        panel.add(new JLabel(), GridBagHelper.rightCol());
        panel.add(isFixedIntensityCheckBox, GridBagHelper.leftCol());
        panel.add(expectedIntensityTextField, GridBagHelper.rightCol());

        params.loadPrefs();
        return panel;
    }

    public void readParameters() {
        params.readDialogOptions();
        params.savePrefs();
    }

    public void recordOptions() {
        params.recordMacroOptions();
    }

    public void readMacroOptions(String options) {
        params.readMacroOptions();
    }

    public void resetToDefaults() {
        params.resetToDefaults(true);
    }

    IEstimator getMLEImplementation(PSFModel psf, double sigma, int fitradius) {
        Range intensityRange = params.getBoolean(FIXED_INTENSITY) ? Range.parseFromTo(params.getString(INTENSITY_RANGE)) : null;
        MFA_MLEFitter fitter = new MFA_MLEFitter(psf, sigma, params.getInt(NMAX), params.getDouble(PVALUE), params.getBoolean(KEEP_SAME_INTENSITY), intensityRange);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    IEstimator getLSQImplementation(PSFModel psf, double sigma, int fitradius) {
        Range intensityRange = params.getBoolean(FIXED_INTENSITY) ? Range.parseFromTo(params.getString(INTENSITY_RANGE)) : null;
        MFA_LSQFitter fitter = new MFA_LSQFitter(psf, sigma, params.getInt(NMAX), params.getDouble(PVALUE), params.getBoolean(KEEP_SAME_INTENSITY), intensityRange);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }
}
