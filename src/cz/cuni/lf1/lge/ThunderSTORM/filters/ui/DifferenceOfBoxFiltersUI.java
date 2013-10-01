package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfBoxFilters;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfBoxFiltersUI extends IFilterUI {

    private final String name = "Difference of box (mean) filters";
    public transient static final int DEFAULT_SIZE1 = 3;
    public transient static final int DEFAULT_SIZE2 = 6;
    private transient static final ParameterName.Integer SIZE1 = new ParameterName.Integer("size1");
    private transient static final ParameterName.Integer SIZE2 = new ParameterName.Integer("size2");

    public DifferenceOfBoxFiltersUI() {
        parameters.createIntField(SIZE1, IntegerValidatorFactory.positiveNonZero(), DEFAULT_SIZE1);
        parameters.createIntField(SIZE2, IntegerValidatorFactory.positiveNonZero(), DEFAULT_SIZE2);
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
        parameters.registerComponent(SIZE1, sizeTextField1);
        parameters.registerComponent(SIZE2, sizeTextField2);
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
        return new DifferenceOfBoxFilters(parameters.getInt(SIZE1), parameters.getInt(SIZE2));
    }
}
