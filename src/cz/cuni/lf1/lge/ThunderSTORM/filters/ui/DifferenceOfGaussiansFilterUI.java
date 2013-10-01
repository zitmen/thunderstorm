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

    private final String name = "Difference of Gaussians";
    private transient static final int DEFAULT_SIZE = 11;
    private transient static final double DEFAULT_SIGMA_G1 = 1.6;
    private transient static final double DEFAULT_SIGMA_G2 = 1;
    private transient static final ParameterName.Integer SIZE = new ParameterName.Integer("size");
    private transient static final ParameterName.Double SIGMA_G1 = new ParameterName.Double("sigma1");
    private transient static final ParameterName.Double SIGMA_G2 = new ParameterName.Double("sigma2");

    public DifferenceOfGaussiansFilterUI() {
        parameters.createIntField(SIZE, IntegerValidatorFactory.positiveNonZero(), DEFAULT_SIZE);
        parameters.createDoubleField(SIGMA_G1, DoubleValidatorFactory.positiveNonZero(), DEFAULT_SIGMA_G1);
        parameters.createDoubleField(SIGMA_G2, DoubleValidatorFactory.positiveNonZero(), DEFAULT_SIGMA_G2);
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
        parameters.registerComponent(SIZE, sizeTextField);
        parameters.registerComponent(SIGMA_G1, sigma1TextField);
        parameters.registerComponent(SIGMA_G2, sigma2TextField);
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
        return new DifferenceOfGaussiansFilter(parameters.getInt(SIZE), parameters.getDouble(SIGMA_G1), parameters.getDouble(SIGMA_G2));
    }
}
