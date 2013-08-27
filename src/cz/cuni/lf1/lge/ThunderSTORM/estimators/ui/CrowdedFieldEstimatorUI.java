package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MFA_LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CrowdedFieldEstimatorUI implements IEstimatorUI {
    
    private final String name = "Milti-emitter analysis";
    protected int fitradius;
    protected int nmax;
    protected double sigma;
    protected double pvalue;
    protected transient JTextField fitregsizeTextField;
    protected transient JTextField sigmaTextField;
    protected transient JTextField nMaxTextField;
    protected transient JTextField pValueTextField;
    protected transient int DEFAULT_FITRAD = 13;
    protected transient int DEFAULT_NMAX = 5;
    protected transient double DEFAULT_SIGMA = 1.6;
    protected transient double DEFAULT_PVALUE = 1e-6;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        fitregsizeTextField = new JTextField(Prefs.get("thunderstorm.estimators.fitregion", "" + DEFAULT_FITRAD), 20);
        sigmaTextField = new JTextField(Prefs.get("thunderstorm.estimators.sigma", "" + DEFAULT_SIGMA));
        nMaxTextField = new JTextField(Prefs.get("thunderstorm.estimators.nmax", "" + DEFAULT_NMAX));
        pValueTextField = new JTextField(Prefs.get("thunderstorm.estimators.ovalue", "" + DEFAULT_PVALUE));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Fitting radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Initial sigma [px]:"), GridBagHelper.leftCol());
        panel.add(sigmaTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Maximum of molecules per fitting region:"), GridBagHelper.leftCol());
        panel.add(nMaxTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Threshold for more complicated model (p-value):"), GridBagHelper.leftCol());
        panel.add(pValueTextField, GridBagHelper.rightCol());

        return panel;
    }

    @Override
    public void readParameters() {
        fitradius = Integer.parseInt(fitregsizeTextField.getText());
        sigma = Double.parseDouble(sigmaTextField.getText());
        nmax = Integer.parseInt(nMaxTextField.getText());
        pvalue = Double.parseDouble(pValueTextField.getText());

        Prefs.set("thunderstorm.estimators.fitregion", "" + fitradius);
        Prefs.set("thunderstorm.estimators.sigma", "" + sigma);
        Prefs.set("thunderstorm.estimators.nmax", "" + nmax);
        Prefs.set("thunderstorm.estimators.pvalue", "" + pvalue);
    }

    @Override
    public IEstimator getImplementation() {
        MFA_LSQFitter fitter = new MFA_LSQFitter(new IntegratedSymmetricGaussianPSF(sigma), sigma, nmax, pvalue);
        return new MultipleLocationsImageFitting(fitradius, fitter);
    }

    @Override
    public void recordOptions() {
        if(fitradius != DEFAULT_FITRAD) {
            Recorder.recordOption("fitrad", Integer.toString(fitradius));
        }
        if(sigma != DEFAULT_SIGMA) {
            Recorder.recordOption("sigma", Double.toString(sigma));
        }
        if(nmax != DEFAULT_NMAX) {
            Recorder.recordOption("nmax", Integer.toString(nmax));
        }
        if(pvalue != DEFAULT_PVALUE) {
            Recorder.recordOption("pvalue", Double.toString(pvalue));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        fitradius = Integer.parseInt(Macro.getValue(options, "fitrad", Integer.toString(DEFAULT_FITRAD)));
        sigma = Double.parseDouble(Macro.getValue(options, "sigma", Double.toString(DEFAULT_SIGMA)));
        nmax = Integer.parseInt(Macro.getValue(options, "nmax", Integer.toString(DEFAULT_NMAX)));
        pvalue = Double.parseDouble(Macro.getValue(options, "pvalue", Double.toString(DEFAULT_PVALUE)));
    }

    @Override
    public void resetToDefaults() {
        fitregsizeTextField.setText(Integer.toString(DEFAULT_FITRAD));
        sigmaTextField.setText(Double.toString(DEFAULT_SIGMA));
        nMaxTextField.setText(Double.toString(DEFAULT_NMAX));
        pValueTextField.setText(Double.toString(DEFAULT_PVALUE));
    }

}
