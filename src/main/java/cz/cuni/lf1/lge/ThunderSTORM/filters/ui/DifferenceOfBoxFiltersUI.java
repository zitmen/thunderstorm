package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfBoxFilters;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfBoxFiltersUI extends IFilterUI {

    private final String name = "Difference of averaging filters";
    private int size1, size2;
    private transient ParameterKey.Integer size1Param;
    private transient ParameterKey.Integer size2Param;

    public DifferenceOfBoxFiltersUI() {
        size1Param = parameters.createIntField("size1", IntegerValidatorFactory.positiveNonZero(), 3);
        size2Param = parameters.createIntField("size2", IntegerValidatorFactory.positiveNonZero(), 5);
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
        parameters.registerComponent(size1Param, sizeTextField1);
        parameters.registerComponent(size2Param, sizeTextField2);
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
        return new DifferenceOfBoxFilters(size1 = size1Param.getValue(), size2 = size2Param.getValue());
    }
}
