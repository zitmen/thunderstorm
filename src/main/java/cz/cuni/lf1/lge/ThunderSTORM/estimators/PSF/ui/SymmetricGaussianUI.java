package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.RangeValidatorFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Note: here `sigma` and `z` are not semantically correct; The reason for this
 *       is that using FWMH range we simulate a linear defocus with the lower
 *       value of the range being in focus; this is simply a convenient way of
 *       implementation for data generator; it has no other semantical meaning
 */
public class SymmetricGaussianUI extends IPsfUI {
    
    private final String name = "Gaussian";
    private final transient ParameterKey.String FWHM_RANGE = parameters.createStringField("fwhm_range", RangeValidatorFactory.fromTo(), Defaults.FWHM_RANGE);
    private Range zRange;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField fwhmTextField = new JTextField("", 20);
        parameters.registerComponent(FWHM_RANGE, fwhmTextField);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("FWHM range (from:to) [nm]:"), GridBagHelper.leftCol());
        panel.add(fwhmTextField, GridBagHelper.rightCol());
        
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public PSFModel getImplementation() {
        return new SymmetricGaussianPSF(1.6);
    }

    @Override
    public double getAngle() {
        return 0;
    }

    @Override
    public synchronized Range getZRange() {
        if(zRange == null) {
            zRange = Range.parseFromTo(FWHM_RANGE.getValue(), Units.NANOMETER, Units.PIXEL);
            zRange.from = fwhm2sigma(zRange.from);
            zRange.to = fwhm2sigma(zRange.to);
        }
        return zRange;
    }

    @Override
    public double getSigma1(double z) {
        if(!zRange.isIn(z)) {
            return Double.NaN;
        }
        return z;
    }

    @Override
    public double getSigma2(double z) {
        return getSigma1(z);
    }

    @Override
    public boolean is3D() {
        return false;
    }
    
    static class Defaults {
        public static final String FWHM_RANGE = "200:350";
    }
    
}
