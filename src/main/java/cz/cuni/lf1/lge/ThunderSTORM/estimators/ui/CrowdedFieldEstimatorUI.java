package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.Validator;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.ValidatorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CrowdedFieldEstimatorUI {

    private final String name = "Multi-emitter fitting analysis";
    private boolean mfaEnabled;
    private int nMax;
    private double pValue;
    private boolean keepSameIntensity;
    private boolean intensityInRange;
    private String intensityRange;
    //parameters
    protected transient ParameterTracker params;
    protected transient ParameterKey.Boolean ENABLED;
    protected transient ParameterKey.Integer NMAX;
    protected transient ParameterKey.Double PVALUE;
    protected transient ParameterKey.Boolean KEEP_SAME_INTENSITY;
    protected transient ParameterKey.Boolean FIXED_INTENSITY;
    protected transient ParameterKey.String INTENSITY_RANGE;

    public CrowdedFieldEstimatorUI() {
        params = new ParameterTracker("thunderstorm.estimators.dense.mfa");
        ParameterTracker.Condition enabledCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return ENABLED.getValue();
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{ENABLED};
            }
        };
        ParameterTracker.Condition enabledAndFixedIntensityCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return ENABLED.getValue() && FIXED_INTENSITY.getValue();
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{ENABLED, FIXED_INTENSITY};
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

    OneLocationFitter getMLEImplementation(PSFModel psf, double sigma) {
        loadValues();
        Range intensityRange = isFixedIntensity() ? getIntensityRange() : null;
        MFA_MLEFitter fitter = new MFA_MLEFitter(psf, sigma, getMaxMolecules(), getPValue(), isKeepSameIntensity(), intensityRange);
        return fitter;
        //return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    private void loadValues() {
        intensityInRange = isFixedIntensity();
        intensityRange = INTENSITY_RANGE.getValue();
        nMax = getMaxMolecules();
        pValue = getPValue();
        keepSameIntensity = isKeepSameIntensity();
        mfaEnabled = isEnabled();
    }

    OneLocationFitter getLSQImplementation(PSFModel psf, double sigma) {
        Range intensityRange = isFixedIntensity() ? getIntensityRange() : null;
        MFA_LSQFitter fitter = new MFA_LSQFitter(psf, sigma, getMaxMolecules(), getPValue(), isKeepSameIntensity(), intensityRange);
        return fitter;
        //return new MultipleLocationsImageFitting(fitradius, fitter);
    }
    
    public boolean isFixedIntensity() {
        return FIXED_INTENSITY.getValue();
    }

    public Range getIntensityRange() {
        return Range.parseFromTo(INTENSITY_RANGE.getValue());
    }
    
    public int getMaxMolecules() {
        return NMAX.getValue();
    }
    
    public double getPValue() {
        return PVALUE.getValue();
    }

    public boolean isKeepSameIntensity() {
        return KEEP_SAME_INTENSITY.getValue();
    }
}
