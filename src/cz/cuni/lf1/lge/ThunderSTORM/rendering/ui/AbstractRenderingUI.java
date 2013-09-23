package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.ImagePlus;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class AbstractRenderingUI extends IRendererUI {
  
  double magnification;
  int sizeX;
  int sizeY;
  int repaintFrequency;
  double zFrom, zTo, zStep;
  boolean threeD = false;
  JTextField resolutionTextField, repaintFrequencyTextField, zRangeTextField;
  JLabel zRangeLabel;
  JCheckBox threeDCheckBox;
  ImagePlus image;            //must be set in subclass
  private final static double DEFAULT_MAGNIFICATION = 5;
  private final static int DEFAULT_REPAINT_FREQUENCY = 50;
  private static final String DEFAULT_Z_RANGE = "-500:100:500";
  
  private void defaultInit() {
      magnification = DEFAULT_MAGNIFICATION;
      repaintFrequency = DEFAULT_REPAINT_FREQUENCY;
      String [] range = DEFAULT_Z_RANGE.split(":");
      zFrom = Integer.parseInt(range[0]);
      zStep = Integer.parseInt(range[1]);
      zTo = Integer.parseInt(range[2]);
  }
  
  public AbstractRenderingUI() {
      defaultInit();
  }

  public AbstractRenderingUI(int sizeX, int sizeY) {
      defaultInit();
    this.sizeX = sizeX;
    this.sizeY = sizeY;
  }

  @Override
  public void setSize(int sizeX, int sizeY) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
  }

  @Override
  public int getRepaintFrequency() {
    return repaintFrequency;
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());

    resolutionTextField = new JTextField(Prefs.get("thunderstorm.rendering.resolution", "" + DEFAULT_MAGNIFICATION), 20);
    repaintFrequencyTextField = new JTextField(Prefs.get("thunderstorm.rendering.repaint" , "" + DEFAULT_REPAINT_FREQUENCY), 20);
    panel.add(new JLabel("Magnification:"), GridBagHelper.leftCol());
    panel.add(resolutionTextField, GridBagHelper.rightCol());

    panel.add(new JLabel("Repaint frequency [frames]:"), GridBagHelper.leftCol());
    panel.add(repaintFrequencyTextField, GridBagHelper.rightCol());

    threeD = Prefs.get("thunderstorm.rendering.z", false);
    threeDCheckBox = new JCheckBox("", threeD);
    threeDCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zRangeLabel.setEnabled(threeDCheckBox.isSelected());
        zRangeTextField.setEnabled(threeDCheckBox.isSelected());
      }
    });
    panel.add(new JLabel("3D:"), GridBagHelper.leftCol());
    panel.add(threeDCheckBox, GridBagHelper.rightCol());

    zRangeLabel = new JLabel("Z range (from:step:to) [nm]:");
    panel.add(zRangeLabel, GridBagHelper.leftCol());
    zRangeTextField = new JTextField(Prefs.get("thunderstorm.rendering.zrange", DEFAULT_Z_RANGE), 20);
    zRangeLabel.setEnabled(threeD);
    zRangeTextField.setEnabled(threeD);
    panel.add(zRangeTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    threeD = threeDCheckBox.isSelected();
    if (threeD) {
      setZRange(zRangeTextField.getText());
    }
    magnification = Double.parseDouble(resolutionTextField.getText());
    repaintFrequency = Integer.parseInt(repaintFrequencyTextField.getText());

    Prefs.set("thunderstorm.rendering.z", threeD);
    Prefs.set("thunderstorm.rendering.zrange", zRangeTextField.getText());
    Prefs.set("thunderstorm.rendering.resolution", "" + magnification);
    Prefs.set("thunderstorm.rendering.repaint", "" + repaintFrequency);
  }

  @Override
  public void recordOptions() {
    if (threeD) {
      Recorder.recordOption("zRange", zFrom + ":" + zStep + ":" + zTo);
    }
    if (magnification != DEFAULT_MAGNIFICATION) {
      Recorder.recordOption("resolution", Double.toString(magnification));
    }
    if (repaintFrequency != DEFAULT_REPAINT_FREQUENCY) {
      Recorder.recordOption("repaintFrequency", Integer.toString(repaintFrequency));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    String rangeText = Macro.getValue(options, "zRange", null);
    if (rangeText == null) {
      threeD = false;
    } else {
      threeD = true;
      setZRange(rangeText);
    }
    magnification = Double.parseDouble(Macro.getValue(options, "resolution", "" + DEFAULT_MAGNIFICATION));
    repaintFrequency = Integer.parseInt(Macro.getValue(options, "repaintFrequency", "" + DEFAULT_REPAINT_FREQUENCY));
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {
    return getMethod();
  }

  @Override
  public void resetToDefaults() {
    repaintFrequencyTextField.setText("" + DEFAULT_REPAINT_FREQUENCY);
    resolutionTextField.setText(""+ DEFAULT_MAGNIFICATION);
    threeDCheckBox.setSelected(false);
    zRangeTextField.setText(DEFAULT_Z_RANGE);
    zRangeTextField.setEnabled(false);
  }

  protected abstract IncrementalRenderingMethod getMethod();
  
  private void setZRange(String rangeText) {
    Range r = Range.parseFromStepTo(rangeText);
    zTo = r.to; zFrom = r.from; zStep = r.step;
    int nSlices = (int) ((zTo - zFrom) / zStep);
    if (zFrom > zTo) {
        throw new RuntimeException("Z range \"from\" value (" + zFrom + ") must be smaller than \"to\" value (" + zTo + ").");
    }
    if (nSlices < 1) {
        throw new RuntimeException("Invalid range: Must have at least one slice.");
    }
    zTo = nSlices * zStep + zFrom;
  }

}
