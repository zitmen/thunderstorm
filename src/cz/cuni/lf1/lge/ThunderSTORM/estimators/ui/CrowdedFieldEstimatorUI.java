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
    //parameters
    protected transient ParameterName.Boolean ENABLED;
    protected transient ParameterName.Integer NMAX;
    protected transient ParameterName.Double PVALUE;
    protected transient ParameterName.Boolean KEEP_SAME_INTENSITY;
    protected transient ParameterName.Boolean FIXED_INTENSITY;
    protected transient ParameterName.String INTENSITY_RANGE;

    public CrowdedFieldEstimatorUI() {
        params = new ParameterTracker("thunderstorm.estimators.dense.mfa");
        ParameterTracker.Condition enabledCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return ENABLED.getValue();
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{ENABLED};
            }
        };
        ParameterTracker.Condition enabledAndFixedIntensityCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return ENABLED.getValue() && FIXED_INTENSITY.getValue();
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{ENABLED, FIXED_INTENSITY};
            }
        };
        ENABLED = params.createBooleanField("mfaenabled", null, false);
        NMAX = params.createIntField("nmax", IntegerValidatorFactory.positive(), 5, enabledCondition);
        PVALUE = params.createDoubleField("pvalue", DoubleValidatorFactory.positive(), 1e-6, enabledCondition);
        KEEP_SAME_INTENSITY = params.createBooleanField("keep_same_intensity", null, true, enabledCondition);
        FIXED_INTENSITY = params.createBooleanField("fixed_intensity", null, false, enabledCondition);
        INTENSITY_RANGE = params.createStringField("expected_intensity", new Validator<String>() {
            @Override
            public void validate(String input) throws ValidatorException {
                try {
                    Range r = Range.parseFromTo(input);
                } catch(RuntimeException ex) {
                    throw new ValidatorException(ex);
                }
            }
        }, "500:2500", enabledAndFixedIntensityCondition);
    }

    public boolean isEnabled() {
        return ENABLED.getValue();
    }

    public JPanel getOptionsPanel(JPanel panel) {
        final JCheckBox isEnabledCheckbox = new JCheckBox("enable", true);
        final JTextField nMaxTextField = new JTextField("");
        final JLabel nMaxLabel = new JLabel("Maximum of molecules per fitting region:");
        final JTextField pValueTextField = new JTextField("");
        final JLabel pValueLabel = new JLabel("Model selection threshold (p-value):");
        final JCheckBox keepSameIntensityCheckBox = new JCheckBox("Same intensity for all molecules", false);
        final JCheckBox isFixedIntensityCheckBox = new JCheckBox("Limit intensity range [photons]:", true);
        final JTextField expectedIntensityTextField = new JTextField("");
        isEnabledCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nMaxTextField.setEnabled(isEnabledCheckbox.isSelected());
                nMaxLabel.setEnabled(isEnabledCheckbox.isSelected());
                pValueTextField.setEnabled(isEnabledCheckbox.isSelected());
                pValueLabel.setEnabled(isEnabledCheckbox.isSelected());
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
        panel.add(nMaxLabel, GridBagHelper.leftCol());
        panel.add(nMaxTextField, GridBagHelper.rightCol());
        panel.add(pValueLabel, GridBagHelper.leftCol());
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
        Range intensityRange = FIXED_INTENSITY.getValue() ? Range.parseFromTo(INTENSITY_RANGE.getValue()) : null;
        MFA_MLEFitter fitter = new MFA_MLEFitter(psf, sigma, NMAX.getValue(), PVALUE.getValue(), KEEP_SAME_INTENSITY.getValue(), intensityRange);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    IEstimator getLSQImplementation(PSFModel psf, double sigma, int fitradius) {
        Range intensityRange = FIXED_INTENSITY.getValue() ? Range.parseFromTo(INTENSITY_RANGE.getValue()) : null;
        MFA_LSQFitter fitter = new MFA_LSQFitter(psf, sigma, NMAX.getValue(), PVALUE.getValue(), KEEP_SAME_INTENSITY.getValue(), intensityRange);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }
}
