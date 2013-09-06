package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CompoundWaveletFilterUI extends IFilterUI {

    private final String name = "Wavelet filter";
    private boolean third_plane = false;
    private transient JCheckBox thirdCheckBox;
    private transient static final boolean DEFAULT_THIRD_PLANE = false;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        thirdCheckBox = new JCheckBox("use third plane");
        thirdCheckBox.setSelected(Prefs.get("thunderstorm.filters.wave.thirdplane", DEFAULT_THIRD_PLANE));
        //
        JPanel panel = new JPanel();
        panel.add(thirdCheckBox);
        return panel;
    }

    @Override
    public void readParameters() {
        third_plane = thirdCheckBox.isSelected();

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
        thirdCheckBox.setSelected(DEFAULT_THIRD_PLANE);
    }
}
