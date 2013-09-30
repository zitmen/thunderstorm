package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CrowdedFieldEstimatorUI implements ActionListener {

    private final String name = "Multi-emitter fitting analysis";
    protected int nmax;
    protected double pvalue;
    protected boolean enabled;
    protected boolean fixedIntensity;
    protected boolean keepSameIntensity;
    protected String expectedIntensityStr;
    protected transient Range expectedIntensity;
    protected transient JCheckBox isEnabledCheckbox;
    protected transient JCheckBox isFixedIntensityCheckBox;
    protected transient JCheckBox keepSameIntensityCheckBox;
    protected transient JTextField nMaxTextField;
    protected transient JTextField pValueTextField;
    protected transient JTextField expectedIntensityTextField;
    protected transient boolean DEFAULT_ENABLED = false;
    protected transient int DEFAULT_NMAX = 5;
    protected transient double DEFAULT_PVALUE = 1e-6;
    protected transient boolean DEFAULT_FIXED_INTENSITY = false;
    protected transient boolean DEFAULT_KEEP_SAME_INTENSITY = true;
    protected transient String DEFAULT_INTENSITY_RANGE = "500:2500";

    public boolean isEnabled() {
        return enabled;
    }

    public JPanel getOptionsPanel(JPanel panel) {
        enabled = Boolean.parseBoolean(Prefs.get("thunderstorm.estimators.dense.mfa.enabled", Boolean.toString(DEFAULT_ENABLED)));
        isEnabledCheckbox = new JCheckBox("enable", enabled);
        isEnabledCheckbox.addActionListener(this);
        nMaxTextField = new JTextField(Prefs.get("thunderstorm.estimators.dense.mfa.nmax", "" + DEFAULT_NMAX));
        nMaxTextField.setEnabled(enabled);
        pValueTextField = new JTextField(Prefs.get("thunderstorm.estimators.dense.mfa.pvalue", "" + DEFAULT_PVALUE));
        pValueTextField.setEnabled(enabled);
        keepSameIntensity = Boolean.parseBoolean(Prefs.get("thunderstorm.estimators.dense.mfa.keep_same_intensity", Boolean.toString(DEFAULT_KEEP_SAME_INTENSITY)));
        keepSameIntensityCheckBox = new JCheckBox("Keep the same intensity of all molecules", keepSameIntensity);
        fixedIntensity = Boolean.parseBoolean(Prefs.get("thunderstorm.estimators.dense.mfa.fixed_intensity", Boolean.toString(DEFAULT_FIXED_INTENSITY)));
        isFixedIntensityCheckBox = new JCheckBox("Fix intensity to the range [photons]:", fixedIntensity);
        isFixedIntensityCheckBox.addActionListener(this);
        isFixedIntensityCheckBox.setEnabled(enabled);
        expectedIntensityTextField = new JTextField(Prefs.get("thunderstorm.estimators.dense.mfa.expected_intensity", "" + DEFAULT_INTENSITY_RANGE));
        expectedIntensityTextField.setEnabled(enabled && fixedIntensity);

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

        return panel;
    }

    public void readParameters() {
        enabled = isEnabledCheckbox.isSelected();
        Prefs.set("thunderstorm.estimators.dense.mfa.enabled", Boolean.toString(enabled));
        
        if(enabled) {
            nmax = Integer.parseInt(nMaxTextField.getText());
            pvalue = Double.parseDouble(pValueTextField.getText());
            expectedIntensityStr = expectedIntensityTextField.getText();
            expectedIntensity = Range.parseFromTo(expectedIntensityStr);
            expectedIntensity.convert(Units.PHOTON, Units.DIGITAL);
            fixedIntensity = isFixedIntensityCheckBox.isSelected();
            keepSameIntensity = keepSameIntensityCheckBox.isSelected();
            Prefs.set("thunderstorm.estimators.dense.mfa.nmax", "" + nmax);
            Prefs.set("thunderstorm.estimators.dense.mfa.pvalue", "" + pvalue);
            Prefs.set("thunderstorm.estimators.dense.mfa.fixed_intensity", Boolean.toString(fixedIntensity));
            Prefs.set("thunderstorm.estimators.dense.mfa.keep_same_intensity", Boolean.toString(keepSameIntensity));
            if(fixedIntensity) {
                Prefs.set("thunderstorm.estimators.dense.mfa.expected_intensity", "" + expectedIntensityStr);
            }
        }
        
    }

    public void recordOptions() {
        Recorder.recordOption("enabled", Boolean.toString(enabled));
        Recorder.recordOption("fixed_intensity", Boolean.toString(fixedIntensity));
        Recorder.recordOption("keep_same_intensity", Boolean.toString(keepSameIntensity));
        if(nmax != DEFAULT_NMAX && enabled) {
            Recorder.recordOption("nmax", Integer.toString(nmax));
        }
        if(pvalue != DEFAULT_PVALUE && enabled) {
            Recorder.recordOption("pvalue", Double.toString(pvalue));
        }
        if(!DEFAULT_INTENSITY_RANGE.equals(expectedIntensityStr) && enabled && fixedIntensity) {
            Recorder.recordOption("expected_intensity", expectedIntensityStr);
        }
    }

    public void readMacroOptions(String options) {
        enabled = Boolean.parseBoolean(Macro.getValue(options, "enabled", Boolean.toString(DEFAULT_ENABLED)));
        fixedIntensity = Boolean.parseBoolean(Macro.getValue(options, "fixed_intensity", Boolean.toString(DEFAULT_FIXED_INTENSITY)));
        keepSameIntensity = Boolean.parseBoolean(Macro.getValue(options, "keep_same_intensity", Boolean.toString(DEFAULT_KEEP_SAME_INTENSITY)));
        nmax = Integer.parseInt(Macro.getValue(options, "nmax", Integer.toString(DEFAULT_NMAX)));
        pvalue = Double.parseDouble(Macro.getValue(options, "pvalue", Double.toString(DEFAULT_PVALUE)));
        expectedIntensityStr = Macro.getValue(options, "expected_intensity", DEFAULT_INTENSITY_RANGE);
        expectedIntensity = Range.parseFromTo(expectedIntensityStr);
    }

    public void resetToDefaults() {
        nMaxTextField.setText(Integer.toString(DEFAULT_NMAX));
        pValueTextField.setText(Double.toString(DEFAULT_PVALUE));
        expectedIntensityTextField.setText(DEFAULT_INTENSITY_RANGE);
        isEnabledCheckbox.setSelected(DEFAULT_ENABLED);
        isFixedIntensityCheckBox.setSelected(DEFAULT_FIXED_INTENSITY);
        keepSameIntensityCheckBox.setSelected(DEFAULT_KEEP_SAME_INTENSITY);
        nMaxTextField.setEnabled(DEFAULT_ENABLED);
        pValueTextField.setEnabled(DEFAULT_ENABLED);
        isFixedIntensityCheckBox.setEnabled(DEFAULT_ENABLED);
        keepSameIntensityCheckBox.setEnabled(DEFAULT_ENABLED);
        expectedIntensityTextField.setEnabled(DEFAULT_ENABLED && DEFAULT_FIXED_INTENSITY);
    }

    IEstimator getMLEImplementation(PSFModel psf, double sigma, int fitradius) {
        MFA_MLEFitter fitter = new MFA_MLEFitter(psf, sigma, nmax, pvalue, keepSameIntensity, fixedIntensity ? expectedIntensity : null);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    IEstimator getLSQImplementation(PSFModel psf, double sigma, int fitradius) {
        MFA_LSQFitter fitter = new MFA_LSQFitter(psf, sigma, nmax, pvalue, keepSameIntensity, fixedIntensity ? expectedIntensity : null);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == isEnabledCheckbox) {
            nMaxTextField.setEnabled(isEnabledCheckbox.isSelected());
            pValueTextField.setEnabled(isEnabledCheckbox.isSelected());
            isFixedIntensityCheckBox.setEnabled(isEnabledCheckbox.isSelected());
            keepSameIntensityCheckBox.setEnabled(isEnabledCheckbox.isSelected());
            expectedIntensityTextField.setEnabled(isEnabledCheckbox.isSelected() && isFixedIntensityCheckBox.isSelected());
        }
        if(e.getSource() == isFixedIntensityCheckBox) {
            expectedIntensityTextField.setEnabled(isEnabledCheckbox.isSelected() && isFixedIntensityCheckBox.isSelected());
        }
    }
}
