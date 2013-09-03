package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_MLEFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
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
    protected transient JCheckBox isEnabledCheckbox;
    protected transient JTextField nMaxTextField;
    protected transient JTextField pValueTextField;
    protected transient boolean DEFAULT_ENABLED = false;
    protected transient int DEFAULT_NMAX = 5;
    protected transient double DEFAULT_PVALUE = 1e-6;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public JPanel getOptionsPanel(JPanel panel) {
        isEnabledCheckbox = new JCheckBox("enable", Boolean.parseBoolean(Prefs.get("thunderstorm.estimators.dense.mfa.enabled", Boolean.toString(DEFAULT_ENABLED))));
        isEnabledCheckbox.addActionListener(this);
        nMaxTextField = new JTextField(Prefs.get("thunderstorm.estimators.dense.mfa.nmax", "" + DEFAULT_NMAX));
        nMaxTextField.setEnabled(enabled);
        pValueTextField = new JTextField(Prefs.get("thunderstorm.estimators.dense.mfa.pvalue", "" + DEFAULT_PVALUE));
        pValueTextField.setEnabled(enabled);

        panel.add(new JLabel(name + ":"), GridBagHelper.leftCol());
        panel.add(isEnabledCheckbox, GridBagHelper.rightCol());
        panel.add(new JLabel("Maximum of molecules per fitting region:"), GridBagHelper.leftCol());
        panel.add(nMaxTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Threshold for more complicated model (p-value):"), GridBagHelper.leftCol());
        panel.add(pValueTextField, GridBagHelper.rightCol());

        return panel;
    }

    public void readParameters() {
        enabled = isEnabledCheckbox.isSelected();
        nmax = Integer.parseInt(nMaxTextField.getText());
        pvalue = Double.parseDouble(pValueTextField.getText());

        Prefs.set("thunderstorm.estimators.dense.mfa.enabled", Boolean.toString(enabled));
        Prefs.set("thunderstorm.estimators.dense.mfa.nmax", "" + nmax);
        Prefs.set("thunderstorm.estimators.dense.mfa.pvalue", "" + pvalue);
    }

    public void recordOptions() {
        if(enabled != DEFAULT_ENABLED) {
            Recorder.recordOption("enabled", Boolean.toString(enabled));
        }
        if(nmax != DEFAULT_NMAX) {
            Recorder.recordOption("nmax", Integer.toString(nmax));
        }
        if(pvalue != DEFAULT_PVALUE) {
            Recorder.recordOption("pvalue", Double.toString(pvalue));
        }
    }

    public void readMacroOptions(String options) {
        enabled = Boolean.parseBoolean(Macro.getValue(options, "enabled", Boolean.toString(DEFAULT_ENABLED)));
        nmax = Integer.parseInt(Macro.getValue(options, "nmax", Integer.toString(DEFAULT_NMAX)));
        pvalue = Double.parseDouble(Macro.getValue(options, "pvalue", Double.toString(DEFAULT_PVALUE)));
    }

    public void resetToDefaults() {
        isEnabledCheckbox.setSelected(DEFAULT_ENABLED);
        nMaxTextField.setText(Double.toString(DEFAULT_NMAX));
        pValueTextField.setText(Double.toString(DEFAULT_PVALUE));
    }

    IEstimator getMLEImplementation(PSFModel psf, double sigma, int fitradius) {
        MFA_LSQFitter fitter = new MFA_LSQFitter(new IntegratedSymmetricGaussianPSF(sigma), sigma, nmax, pvalue);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    IEstimator getLSQImplementation(PSFModel psf, double sigma, int fitradius) {
        MFA_MLEFitter fitter = new MFA_MLEFitter(psf, sigma, nmax, pvalue);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == isEnabledCheckbox) {
            nMaxTextField.setEnabled(isEnabledCheckbox.isSelected());
            pValueTextField.setEnabled(isEnabledCheckbox.isSelected());
        }
    }

}
