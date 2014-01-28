package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfBoxFilters;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfBoxFiltersUI extends IFilterUI {

    private final String name = "Difference of averaging filters";
    private transient ParameterKey.Integer size1;
    private transient ParameterKey.Integer size2;

    public DifferenceOfBoxFiltersUI() {
        size1 = parameters.createIntField("size1", IntegerValidatorFactory.positiveNonZero(), 3);
        size2 = parameters.createIntField("size2", IntegerValidatorFactory.positiveNonZero(), 5);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".box";
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JTextField sizeTextField1 = new JTextField("", 20);
        JTextField sizeTextField2 = new JTextField("", 20);
        parameters.registerComponent(size1, sizeTextField1);
        parameters.registerComponent(size2, sizeTextField2);
        //
        panel.add(new JLabel("First kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField1, GridBagHelper.rightCol());
        panel.add(new JLabel("Second kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField2, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new DifferenceOfBoxFilters(size1.getValue(), size2.getValue());
    }
}
