package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
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

public class ASHRenderingUI extends AbstractRenderingUI {
  
  JTextField shiftsTextField;
  JTextField zShiftsTextField;
  JLabel zShiftsLabel;
  int shifts;
  int zShifts;
  private static final int DEFAULT_SHIFTS = 2;
  private static final int DEFAULT_ZSHIFTS = 2;
  
  private void defaultInit() {
      shifts = DEFAULT_SHIFTS;
      zShifts = DEFAULT_ZSHIFTS;
  }
  
  public ASHRenderingUI() {
      super();
      defaultInit();
  }
  
  public ASHRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
    defaultInit();
  }
  
  @Override
  public String getName() {
    return "ASH Renderer";
  }
  
  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();
    
    shiftsTextField = new JTextField(Prefs.get("thunderstorm.rendering.ash.shifts",""+DEFAULT_SHIFTS), 20);
    panel.add(new JLabel("Lateral shifts:"), GridBagHelper.leftCol());
    panel.add(shiftsTextField, GridBagHelper.rightCol());
    
    zShiftsTextField = new JTextField(Prefs.get("thunderstorm.rendering.ash.zshifts", ""+DEFAULT_ZSHIFTS), 20);
    zShiftsLabel = new JLabel("Axial shifts:");
    zShiftsLabel.setEnabled(threeD);
    zShiftsTextField.setEnabled(threeD);
    panel.add(zShiftsLabel, GridBagHelper.leftCol());
    panel.add(zShiftsTextField, GridBagHelper.rightCol());
    threeDCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zShiftsLabel.setEnabled(threeDCheckBox.isSelected());
        zShiftsTextField.setEnabled(threeDCheckBox.isSelected());
      }
    });
    
    return panel;
  }
  
  @Override
  public void readParameters() {
    super.readParameters();
    shifts = Integer.parseInt(shiftsTextField.getText());
    if (threeD) {
      zShifts = Integer.parseInt(zShiftsTextField.getText());
    }
    
    Prefs.set("thunderstorm.rendering.ash.shifts", ""+ shifts);
    Prefs.set("thunderstorm.rendering.ash.zshifts", ""+ zShifts);
  }
  
  @Override
  public void recordOptions() {
    super.recordOptions();
    if (shifts != DEFAULT_SHIFTS) {
      Recorder.recordOption("shifts", Integer.toString(shifts));
    }
    if (threeD && zShifts != DEFAULT_ZSHIFTS) {
      Recorder.recordOption("zShifts", Integer.toString(zShifts));
    }
  }

  @Override
  public void resetToDefaults() {
    super.resetToDefaults();
    shiftsTextField.setText(""+DEFAULT_SHIFTS);
    zShiftsTextField.setText(""+DEFAULT_ZSHIFTS);
  }
  
  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
    shifts = Integer.parseInt(Macro.getValue(options, "shifts", Integer.toString(DEFAULT_SHIFTS)));
    if (threeD) {
      zShifts = Integer.parseInt(Macro.getValue(options, "zShifts", Integer.toString(DEFAULT_ZSHIFTS)));
    }
  }
  
  @Override
  public IncrementalRenderingMethod getMethod() {
    if (threeD) {
      return new ASHRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1/magnification).shifts(shifts).zRange(zFrom, zTo, zStep).zShifts(zShifts).build();
    } else {
      return new ASHRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1/magnification).shifts(shifts).build();
    }
  }
}
