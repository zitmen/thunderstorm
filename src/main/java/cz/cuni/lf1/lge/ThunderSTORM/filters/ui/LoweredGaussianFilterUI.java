package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoweredGaussianFilterUI extends IFilterUI {

    private final String name = "Lowered Gaussian filter";
    private double sigma;
    private transient ParameterKey.Double sigmaParam;

    public LoweredGaussianFilterUI() {
        sigmaParam = parameters.createDoubleField("sigma", DoubleValidatorFactory.positiveNonZero(), 1.6);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".lowgauss";
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField sigmaTextField = new JTextField("", 20);
        parameters.registerComponent(sigmaParam, sigmaTextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Sigma [px]: "), GridBagHelper.leftCol());
        panel.add(sigmaTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        sigma = sigmaParam.getValue();
        int size = 1 + 2 * (int) MathProxy.ceil(sigma * 3);
        return new LoweredGaussianFilter(size, sigma);
    }
}
