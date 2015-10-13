package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfGaussiansFilterUI extends IFilterUI {

    private final String name = "Difference-of-Gaussians filter";
    private double sigmaG1, sigmaG2;
    private transient ParameterKey.Double sigmaG1Param;
    private transient ParameterKey.Double sigmaG2Param;

    public DifferenceOfGaussiansFilterUI() {
        sigmaG1Param = parameters.createDoubleField("sigma1", DoubleValidatorFactory.positiveNonZero(), 1);
        sigmaG2Param = parameters.createDoubleField("sigma2", DoubleValidatorFactory.positiveNonZero(), 1.6);
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
        JTextField sigma1TextField = new JTextField("", 20);
        JTextField sigma2TextField = new JTextField("", 20);
        parameters.registerComponent(sigmaG1Param, sigma1TextField);
        parameters.registerComponent(sigmaG2Param, sigma2TextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Sigma1 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma1TextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma2 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma2TextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        sigmaG1 = sigmaG1Param.getValue();
        sigmaG2 = sigmaG2Param.getValue();
        int size = 1 + 2 * (int) MathProxy.ceil(3 * MathProxy.max(sigmaG1, sigmaG2));
        return new DifferenceOfGaussiansFilter(size, sigmaG1, sigmaG2);
    }
}
