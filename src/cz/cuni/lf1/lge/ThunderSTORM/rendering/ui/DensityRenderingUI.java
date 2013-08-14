package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DensityRenderingUI extends AbstractRenderingUI {

  private JTextField dxTextField;
  private JTextField dzTextField;
  private JLabel dzLabel;
  private double dx, dz;
  private static final double DEFAULT_DX = 0.2;
  private static final double DEFAULT_DZ = 2;

  public DensityRenderingUI() {
  }

  public DensityRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();

    panel.add(new JLabel("Lateral resolution [???]:"), GridBagHelper.leftCol());
    dxTextField = new JTextField(Prefs.get("thunderstorm.rendering.density.dx", "" + DEFAULT_DX), 20);
    panel.add(dxTextField, GridBagHelper.rightCol());

    dzLabel = new JLabel("Axial resolution [???]:");
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
    if (threeD) {
      dz = Double.parseDouble(dzTextField.getText());
    }
    
    Prefs.set("thunderstorm.rendering.density.dx", ""+dx);
    if(threeD){
      Prefs.set("thunderstorm.rendering.density.dz", ""+dz);
    }
  }

  @Override
  public void resetToDefaults() {
    super.resetToDefaults();
    dxTextField.setText(""+DEFAULT_DX);
    dzTextField.setText(""+DEFAULT_DZ);
  }

  @Override
  public void recordOptions() {
    super.recordOptions();
    if (dx != DEFAULT_DX) {
      Recorder.recordOption("dx", Double.toString(dx));
    }
    if (dz != DEFAULT_DZ && threeD) {
      Recorder.recordOption("dz", Double.toString(dz));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
    dx = Double.parseDouble(Macro.getValue(options, "dx", Double.toString(DEFAULT_DX)));
    if (threeD) {
      dz = Double.parseDouble(Macro.getValue(options, "dz", Double.toString(DEFAULT_DZ)));
    }
  }

  @Override
  public String getName() {
    return "Density Renderer";
  }

  @Override
  public IncrementalRenderingMethod getMethod() {
    if (threeD) {
      return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).defaultDX(dx).zRange(zFrom, zTo, zStep).defaultDZ(dz).build();
    } else {
      return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).defaultDX(dx).build();
    }
  }
}
