package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoweredGaussianFilterUI extends IFilterUI {

    private final String name = "Lowered Gaussian filter";
    private transient ParameterName.Integer size;
    private transient ParameterName.Double sigma;

    public LoweredGaussianFilterUI() {
        size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 11);
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
        JTextField sizeTextField = new JTextField("", 20);
        JTextField sigmaTextField = new JTextField("", 20);
        parameters.registerComponent(size, sizeTextField);
        parameters.registerComponent(sigma, sigmaTextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma [px]: "), GridBagHelper.leftCol());
        panel.add(sigmaTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new LoweredGaussianFilter(size.getValue(), sigma.getValue());
    }
}
