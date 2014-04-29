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
    private transient ParameterKey.Integer order;
    private transient ParameterKey.Double scale;

    public CompoundWaveletFilterUI() {
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
        parameters.registerComponent(order, orderTextField);
        parameters.registerComponent(scale, scaleTextField);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("B-Spline order: "), GridBagHelper.leftCol());
        panel.add(orderTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("B-Spline scale: "), GridBagHelper.leftCol());
        panel.add(scaleTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        double scaleValue = scale.getValue();
        int orderValue = order.getValue();
        int size = 2 * (int) Math.ceil(orderValue * scaleValue / 2) - 1;
        return new CompoundWaveletFilter(orderValue, scaleValue, size);
    }
}
