package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class CompoundWaveletFilterUI extends IFilterUI {

    private final String name = "Wavelet filter";
    private transient static final ParameterName.Choice PLANE = new ParameterName.Choice("thirdplane");
    private transient static final String secondPlane = "use 2nd wavelet plane";
    private transient static final String thirdPlane = "use 3rd wavelet plane";

    public CompoundWaveletFilterUI() {
        parameters.createChoice(PLANE, null, secondPlane);
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
        JRadioButton secondPlaneRadio = new JRadioButton(secondPlane);
        JRadioButton thirdPlaneRadio = new JRadioButton(thirdPlane);
        ButtonGroup group = new ButtonGroup();
        group.add(secondPlaneRadio);
        group.add(thirdPlaneRadio);
        parameters.registerComponent(PLANE, group);
        //
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(secondPlaneRadio);
        panel.add(thirdPlaneRadio);
        secondPlaneRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
        thirdPlaneRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IFilter getImplementation() {
        return new CompoundWaveletFilter(thirdPlane.equals(parameters.getChoice(PLANE)));
    }
}
