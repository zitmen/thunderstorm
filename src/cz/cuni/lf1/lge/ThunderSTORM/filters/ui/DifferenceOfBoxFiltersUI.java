package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfBoxFilters;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfBoxFiltersUI implements IFilterUI {

    private final String name = "Difference of box (mean) filters";
    private int size1;
    private int size2;
    private transient JTextField sizeTextField1;
    private transient JTextField sizeTextField2;
    public transient static final int DEFAULT_SIZE1 = 3;
    public transient static final int DEFAULT_SIZE2 = 6;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        sizeTextField1 = new JTextField(Prefs.get("thunderstorm.filters.box.size1", "" + DEFAULT_SIZE1), 20);
        sizeTextField2 = new JTextField(Prefs.get("thunderstorm.filters.box.size2", "" + DEFAULT_SIZE2), 20);
        //
        panel.add(new JLabel("First kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField1, GridBagHelper.rightCol());
        panel.add(new JLabel("Second kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField2, GridBagHelper.rightCol());
        return panel;
    }

    @Override
    public void readParameters() {
        size1 = Integer.parseInt(sizeTextField1.getText());
        size2 = Integer.parseInt(sizeTextField2.getText());

        Prefs.set("thunderstorm.filters.box.size1", sizeTextField1.getText());
        Prefs.set("thunderstorm.filters.box.size2", sizeTextField2.getText());
    }

    @Override
    public IFilter getImplementation() {
        return new DifferenceOfBoxFilters(size1, size2);
    }

    @Override
    public void recordOptions() {
        if(size1 != DEFAULT_SIZE1) {
            Recorder.recordOption("size1", Integer.toString(size1));
        }
        if(size2 != DEFAULT_SIZE2) {
            Recorder.recordOption("size2", Integer.toString(size2));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        size1 = Integer.parseInt(Macro.getValue(options, "size1", Integer.toString(DEFAULT_SIZE1)));
        size2 = Integer.parseInt(Macro.getValue(options, "size2", Integer.toString(DEFAULT_SIZE2)));
    }

    @Override
    public void resetToDefaults() {
        sizeTextField1.setText("" + DEFAULT_SIZE1);
        sizeTextField2.setText("" + DEFAULT_SIZE2);
    }
}
