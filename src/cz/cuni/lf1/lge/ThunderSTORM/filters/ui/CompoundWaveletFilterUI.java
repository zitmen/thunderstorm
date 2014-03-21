package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CompoundWaveletFilterUI extends IFilterUI {

    private final String name = "Wavelet filter (B-Spline)";
    private transient ParameterKey.Integer size;
    private transient ParameterKey.Integer order;
    private transient ParameterKey.Double scale;

    public CompoundWaveletFilterUI() {
        size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 5);
        scale = parameters.createDoubleField("scale", DoubleValidatorFactory.positiveNonZero(), 2.0);
        order = parameters.createIntField("order", IntegerValidatorFactory.positiveNonZero(), 3);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".wave";
    }

    @Override
    public JPanel getOptionsPanel() {
        JTextField orderTextField = new JTextField("", 20);
        JTextField scaleTextField = new JTextField("", 20);
        JTextField sizeTextField = new JTextField("", 20);
        parameters.registerComponent(order, orderTextField);
        parameters.registerComponent(scale, scaleTextField);
        parameters.registerComponent(size, sizeTextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("B-Spline order: "), GridBagHelper.leftCol());
        panel.add(orderTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("B-Spline scale: "), GridBagHelper.leftCol());
        panel.add(scaleTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new CompoundWaveletFilter(order.getValue(), scale.getValue(), size.getValue());
    }
}
