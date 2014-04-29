package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class MedianFilterUI extends IFilterUI {

    private final String name = "Median filter";
    private transient ParameterKey.Integer size;
    private transient ParameterKey.String pattern;
    private transient static final String box = "box";
    private transient static final String cross = "cross";

    public MedianFilterUI() {
        size = parameters.createIntField("size", IntegerValidatorFactory.positiveNonZero(), 3);
        pattern = parameters.createStringField("pattern", null, box);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".median";
    }

    @Override
    public JPanel getOptionsPanel() {
        ButtonGroup btnGroup = new ButtonGroup();
        JRadioButton patternBoxRadioButton = new JRadioButton(box);
        JRadioButton patternCrossRadioButton = new JRadioButton(cross);
        btnGroup.add(patternBoxRadioButton);
        btnGroup.add(patternCrossRadioButton);
        JTextField sizeTextField = new JTextField("", 20);
        parameters.registerComponent(size, sizeTextField);
        parameters.registerComponent(pattern, btnGroup);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Pattern: "), GridBagHelper.leftCol());
        panel.add(patternBoxRadioButton, GridBagHelper.rightCol());
        panel.add(patternCrossRadioButton, GridBagHelper.rightCol());

        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new MedianFilter(box.equals(pattern.getValue()) ? MedianFilter.BOX : MedianFilter.CROSS, size.getValue());
    }
}
