package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// TODO: FWHM_RANGE --> udelat analytickej defocus, spocitat Z_RANGE a pak jakoby menit Z v generatoru, stejne jako u elipticky PSF

public class IntegratedSymmetricGaussianUI extends IPsfUI {
    
    private final String name = "Integrated Gaussian";
    private final transient ParameterKey.Double FWHM = parameters.createDoubleField("fwhm", DoubleValidatorFactory.positive(), Defaults.FWHM);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fwhmTextField = new JTextField("", 20);
        parameters.registerComponent(FWHM, fwhmTextField);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("FWHM [nm]:"), GridBagHelper.leftCol());
        panel.add(fwhmTextField, GridBagHelper.rightCol());
        
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public PSFModel getImplementation() {
        return new IntegratedSymmetricGaussianPSF(1.6);
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    public Range getZRange() {
        //return new Range(0, 0);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSigma1(double z) {
        /*
        if(z != 0) {
            throw new IllegalArgumentException("Symmetric Gaussian PSF does not support defocus! Z has to be 0!");
        }
        return fwhm2sigma(FWHM.getValue()); // nm -> px !!
        */
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getSigma2(double z) {
        return getSigma1(z);
    }
    
    static class Defaults {
        public static final double FWHM = 300;
    }
    
}
