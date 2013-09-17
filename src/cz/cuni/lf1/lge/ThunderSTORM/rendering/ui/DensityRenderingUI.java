package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
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

public class DensityRenderingUI extends AbstractRenderingUI {

    private final String name = "Normalized Gaussian";
    private JTextField dxTextField;
    private JTextField dzTextField;
    private JCheckBox forceDXCheckBox;
    private boolean forceDx;
    private JLabel dzLabel;
    private double dx, dz;
    private static final double DEFAULT_DX = 20;
    private static final boolean DEFAULT_FORCE_DX = false;
    private static final double DEFAULT_DZ = 100;

    public DensityRenderingUI() {
    }

    public DensityRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();

        forceDXCheckBox = new JCheckBox("Force", Prefs.get("thunderstorm.rendering.density.forcedx", false));
        JPanel latUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol());
        dxTextField = new JTextField(Prefs.get("thunderstorm.rendering.density.dx", "" + DEFAULT_DX), 10);
        latUncertaintyPanel.add(dxTextField);
        latUncertaintyPanel.add(forceDXCheckBox);
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol());

        dzLabel = new JLabel("Axial uncertainty [nm]:");
        panel.add(dzLabel, GridBagHelper.leftCol());
        dzTextField = new JTextField(Prefs.get("thunderstorm.rendering.density.dz", "" + DEFAULT_DZ), 20);
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
        dx = Double.parseDouble(dxTextField.getText());
        if(threeD) {
            dz = Double.parseDouble(dzTextField.getText());
        }
        forceDx = forceDXCheckBox.isSelected();

        Prefs.set("thunderstorm.rendering.density.forcedx", forceDx);
        Prefs.set("thunderstorm.rendering.density.dx", "" + dx);
        if(threeD) {
            Prefs.set("thunderstorm.rendering.density.dz", "" + dz);
        }
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        forceDXCheckBox.setSelected(DEFAULT_FORCE_DX);
        dxTextField.setText("" + DEFAULT_DX);
        dzTextField.setText("" + DEFAULT_DZ);
    }

    @Override
    public void recordOptions() {
        super.recordOptions();
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

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        dx = Double.parseDouble(Macro.getValue(options, "dx", Double.toString(DEFAULT_DX)));
        if(threeD) {
            dz = Double.parseDouble(Macro.getValue(options, "dz", Double.toString(DEFAULT_DZ)));
        }
        forceDx = Boolean.parseBoolean(Macro.getValue(options, "forcedx", DEFAULT_FORCE_DX + ""));

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(threeD) {
            return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1 / magnification).defaultDX(dx / CameraSetupPlugIn.pixelSize).forceDefaultDX(forceDx).zRange(zFrom, zTo, zStep).defaultDZ(dz).build();
        } else {
            return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1 / magnification).defaultDX(dx / CameraSetupPlugIn.pixelSize).forceDefaultDX(forceDx).build();
        }
    }
}
