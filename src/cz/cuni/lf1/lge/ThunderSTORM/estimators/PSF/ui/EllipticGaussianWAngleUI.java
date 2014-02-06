package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import javax.swing.JPanel;

public class EllipticGaussianWAngleUI extends IPsfUI {

    private final String name = "Eliptical Gaussian (3D astigmatism)";
    //private transient ParameterKey.Integer FITRAD;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        // FWHM(sigma) + angle!
        /*
        JTextField fitregsizeTextField = new JTextField("", 20);
        parameters.registerComponent(FITRAD, fitregsizeTextField);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Estimation radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());

        parameters.loadPrefs();
        return panel;
        */
        return null;
    }

    @Override
    public PSFModel getImplementation() {
        //FITRAD.getValue()
        double defaultSigma = 1.6;
        double fi = 0;
        return new EllipticGaussianWAnglePSF(defaultSigma, fi);
    }
    
}
