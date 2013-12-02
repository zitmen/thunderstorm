package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfGaussiansFilterUI extends IFilterUI {

    private final String name = "Difference-of-Gaussians filter";
    private transient ParameterName.Integer size;
    private transient ParameterName.Double sigmaG1;
    private transient ParameterName.Double sigmaG2;

    public DifferenceOfGaussiansFilterUI() {
        size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 11);
        sigmaG1 = parameters.createDoubleField("sigma1", DoubleValidatorFactory.positiveNonZero(), 1.6);
        sigmaG2 = parameters.createDoubleField("sigma2", DoubleValidatorFactory.positiveNonZero(), 1);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".dog";
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField sizeTextField = new JTextField("", 20);
        JTextField sigma1TextField = new JTextField("", 20);
        JTextField sigma2TextField = new JTextField("", 20);
        parameters.registerComponent(size, sizeTextField);
        parameters.registerComponent(sigmaG1, sigma1TextField);
        parameters.registerComponent(sigmaG2, sigma2TextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma1 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma1TextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma2 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma2TextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new DifferenceOfGaussiansFilter(size.getValue(), sigmaG1.getValue(), sigmaG2.getValue());
    }
}
