package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class HistogramRenderingUI extends AbstractRenderingUI {

    private final String name = "Histograms";
    JTextField avgTextField;
    private JTextField dxTextField;
    private JTextField dzTextField;
    private JLabel dzLabel;
    private JCheckBox forceDXCheckBox;
    private boolean forceDx;
    private double dx, dz;
    int avg;
    private static final int DEFAULT_AVG = 0;
    private static final double DEFAULT_DX = 20;
    private static final boolean DEFAULT_FORCE_DX = false;
    private static final double DEFAULT_DZ = 100;

    public HistogramRenderingUI() {
    }

    public HistogramRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();

        avgTextField = new JTextField(Prefs.get("thunderstorm.rendering.histo.avg", "" + DEFAULT_AVG), 20);
        panel.add(new JLabel("Averages:"), GridBagHelper.leftCol());
        panel.add(avgTextField, GridBagHelper.rightCol());

        forceDXCheckBox = new JCheckBox("Force", Prefs.get("thunderstorm.rendering.histo.forcedx", false));
        JPanel latUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol());
        dxTextField = new JTextField(Prefs.get("thunderstorm.rendering.histo.dx", "" + DEFAULT_DX), 10);
        latUncertaintyPanel.add(dxTextField);
        latUncertaintyPanel.add(forceDXCheckBox);
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol());

        dzLabel = new JLabel("Axial uncertainty [nm]:");
        panel.add(dzLabel, GridBagHelper.leftCol());
        dzTextField = new JTextField(Prefs.get("thunderstorm.rendering.histo.dz", "" + DEFAULT_DZ), 20);
        panel.add(dzTextField, GridBagHelper.rightCol());
        dzLabel.setEnabled(threeD);
        dzTextField.setEnabled(threeD);
        threeDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dzLabel.setEnabled(threeDCheckBox.isSelected());
                dzTextField.setEnabled(threeDCheckBox.isSelected());
            }
        });

        return panel;
    }

    @Override
    public void readParameters() {
        super.readParameters();
        avg = Integer.parseInt(avgTextField.getText());
        if(avg > 0) {
            dx = Double.parseDouble(dxTextField.getText());
            if(threeD) {
                dz = Double.parseDouble(dzTextField.getText());
            }
            forceDx = forceDXCheckBox.isSelected();
        }

        Prefs.set("thunderstorm.rendering.histo.avg", "" + avg);

        if(avg > 0) {
            Prefs.set("thunderstorm.rendering.histo.forcedx", forceDx);
            Prefs.set("thunderstorm.rendering.histo.dx", "" + dx);
            if(threeD) {
                Prefs.set("thunderstorm.rendering.histo.dz", "" + dz);
            }
        }

    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        avgTextField.setText("" + DEFAULT_AVG);
        forceDXCheckBox.setSelected(DEFAULT_FORCE_DX);
        dxTextField.setText("" + DEFAULT_DX);
        dzTextField.setText("" + DEFAULT_DZ);
    }

    @Override
    public void recordOptions() {
        super.recordOptions();
        if(avg != DEFAULT_AVG) {
            Recorder.recordOption("avg", Integer.toString(avg));
        }
        if(avg > 0) {
            if(dx != DEFAULT_DX) {
                Recorder.recordOption("dx", Double.toString(dx));
            }
            if(dz != DEFAULT_DZ && threeD) {
                Recorder.recordOption("dz", Double.toString(dz));
            }
            if(forceDx != DEFAULT_FORCE_DX) {
                Recorder.recordOption("forcedx", forceDx + "");
            }
        }
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        avg = Integer.parseInt(Macro.getValue(options, "avg", Integer.toString(DEFAULT_AVG)));
        dx = Double.parseDouble(Macro.getValue(options, "dx", Double.toString(DEFAULT_DX)));
        if(threeD) {
            dz = Double.parseDouble(Macro.getValue(options, "dz", Double.toString(DEFAULT_DZ)));
        }
        forceDx = Boolean.parseBoolean(Macro.getValue(options, "forcedx", DEFAULT_FORCE_DX + ""));
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(threeD) {
            return new HistogramRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1 / magnification).average(avg).defaultDX(dx / CameraSetupPlugIn.pixelSize).forceDefaultDX(forceDx).defaultDZ(zStep).zRange(zFrom, zTo, zStep).build();
        } else {
            return new HistogramRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1 / magnification).average(avg).defaultDX(dx / CameraSetupPlugIn.pixelSize).forceDefaultDX(forceDx).defaultDZ(zStep).build();
        }
    }
}
