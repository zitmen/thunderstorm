package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CompoundWaveletFilterUI extends IFilterUI {

    private final String name = "Wavelet filter (B-Spline)";
    private double scale;
    private int order;
    private transient ParameterKey.Integer orderParam;
    private transient ParameterKey.Double scaleParam;

    public CompoundWaveletFilterUI() {
        scaleParam = parameters.createDoubleField("scale", DoubleValidatorFactory.positiveNonZero(), 2.0);
        orderParam = parameters.createIntField("order", IntegerValidatorFactory.positiveNonZero(), 3);
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
        parameters.registerComponent(orderParam, orderTextField);
        parameters.registerComponent(scaleParam, scaleTextField);
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
        scale = scaleParam.getValue();
        order = orderParam.getValue();
        int size = 2 * (int) Math.ceil(order * scale / 2) - 1;
        return new CompoundWaveletFilter(order, scale, size);
    }
}
