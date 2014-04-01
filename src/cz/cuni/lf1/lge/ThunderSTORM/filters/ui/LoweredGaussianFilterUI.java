package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoweredGaussianFilterUI extends IFilterUI {

    private final String name = "Lowered Gaussian filter";
    private transient ParameterKey.Double sigma;

    public LoweredGaussianFilterUI() {
        sigma = parameters.createDoubleField("sigma", DoubleValidatorFactory.positiveNonZero(), 1.6);
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
        parameters.registerComponent(sigma, sigmaTextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Sigma [px]: "), GridBagHelper.leftCol());
        panel.add(sigmaTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        double sigmaValue = sigma.getValue();
        int size = 1 + 2 * (int) MathProxy.ceil(sigmaValue * 3);
        return new LoweredGaussianFilter(size, sigmaValue);
    }
}
