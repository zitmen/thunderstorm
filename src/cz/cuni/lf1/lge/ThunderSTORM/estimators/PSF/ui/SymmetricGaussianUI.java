package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import javax.swing.JPanel;

public class SymmetricGaussianUI extends IPsfUI {
    
    private final String name = "Gaussian";

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
        return new SymmetricGaussianPSF(defaultSigma);
    }
    
}
