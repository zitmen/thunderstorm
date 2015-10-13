package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BoxFilterUI extends IFilterUI {

    private final String name = "Averaging (Box) filter";
    private int size;
    private transient ParameterKey.Integer sizeParam;

    public BoxFilterUI() {
        sizeParam = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 3);
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
        JTextField sizeTextField = new JTextField("", 20);
        parameters.registerComponent(sizeParam, sizeTextField);
        //
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new BoxFilter(size = sizeParam.getValue());
    }
}
