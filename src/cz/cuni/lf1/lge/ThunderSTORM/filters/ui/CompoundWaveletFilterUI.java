package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.Component;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class CompoundWaveletFilterUI extends IFilterUI {

    private final String name = "Wavelet filter";
    private boolean third_plane = false;
    private transient JRadioButton secondPlaneRadio, thirdPlaneRadio;
    private transient static final boolean DEFAULT_THIRD_PLANE = false;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        secondPlaneRadio = new JRadioButton("use 2nd wavelet plane");
        thirdPlaneRadio = new JRadioButton("use 3rd wavelet plane");
        ButtonGroup group = new ButtonGroup();
        group.add(secondPlaneRadio);
        group.add(thirdPlaneRadio);
        secondPlaneRadio.setSelected(!Prefs.get("thunderstorm.filters.wave.thirdplane", DEFAULT_THIRD_PLANE));
        thirdPlaneRadio.setSelected(Prefs.get("thunderstorm.filters.wave.thirdplane", DEFAULT_THIRD_PLANE));
        //
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(secondPlaneRadio);
        panel.add(thirdPlaneRadio);
        secondPlaneRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
        thirdPlaneRadio.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    @Override
    public void readParameters() {
        third_plane = thirdPlaneRadio.isSelected();

        Prefs.set("thunderstorm.filters.wave.thirdplane", third_plane);
    }

    @Override
    public IFilter getImplementation() {
        return new CompoundWaveletFilter(third_plane);
    }

    @Override
    public void recordOptions() {
        if(third_plane != DEFAULT_THIRD_PLANE) {
            Recorder.recordOption("third_plane", Boolean.toString(third_plane));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        third_plane = Boolean.parseBoolean(Macro.getValue(options, "third_plane", Boolean.toString(DEFAULT_THIRD_PLANE)));
    }

    @Override
    public void resetToDefaults() {
        secondPlaneRadio.setSelected(!DEFAULT_THIRD_PLANE);
        thirdPlaneRadio.setSelected(DEFAULT_THIRD_PLANE);
    }
}
