package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import javax.swing.JPanel;

public class IntegratedSymmetricGaussianUI extends IPsfUI {
    
    private final String name = "Integrated Gaussian";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        // FWHM(sigma)
        return null;
    }

    @Override
    public PSFModel getImplementation() {
        double defaultSigma = 1.6;
        return new IntegratedSymmetricGaussianPSF(defaultSigma);
    }
    
}
