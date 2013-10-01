package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoweredGaussianFilterUI extends IFilterUI {

    private final String name = "Lowered Gaussian filter";
    private transient static final int DEFAULT_SIZE = 11;
    private transient static final double DEFAULT_SIGMA = 1.6;
    private transient static final ParameterName.Integer SIZE = new ParameterName.Integer("size");
    private transient static final ParameterName.Double SIGMA = new ParameterName.Double("sigma");

    public LoweredGaussianFilterUI() {
        parameters.createIntField(SIZE, IntegerValidatorFactory.positiveNonZero(), DEFAULT_SIZE);
        parameters.createDoubleField(SIGMA, DoubleValidatorFactory.positiveNonZero(), DEFAULT_SIGMA);
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
        parameters.registerComponent(SIZE, sizeTextField);
        parameters.registerComponent(SIGMA, sigmaTextField);
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
        return new LoweredGaussianFilter(parameters.getInt(SIZE), parameters.getDouble(SIGMA));
    }
}
