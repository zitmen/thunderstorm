package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class AbstractRenderingUI implements IRendererUI {

  double resolution;
  int sizeX;
  int sizeY;
  int repaintFrequency;
  double zFrom, zTo, zStep;
  boolean threeD = false;
  JTextField resolutionTextField, repaintFrequencyTextField, zRangeTextField;
  JLabel zRangeLabel;
  JCheckBox threeDCheckBox;
  ImagePlus image;            //must be set in subclass
  Runnable repaint = new Runnable() {
    @Override
    public void run() {
      image.show();
      if (image.isVisible()) {
        IJ.run(image, "Enhance Contrast", "saturated=0.05");
      }
    }
  };
  private final static double DEFAULT_RESOLUTION = 0.2;
  private final static int DEFAULT_REPAINT_FREQUENCY = 20;

  public AbstractRenderingUI() {
  }

  public AbstractRenderingUI(int sizeX, int sizeY) {
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

    resolutionTextField = new JTextField("" + DEFAULT_RESOLUTION, 20);
    repaintFrequencyTextField = new JTextField("" + DEFAULT_REPAINT_FREQUENCY, 20);
    panel.add(new JLabel("Pixel size:"), GridBagHelper.leftCol());
    panel.add(resolutionTextField, GridBagHelper.rightCol());

    panel.add(new JLabel("Repaint frequency:"), GridBagHelper.leftCol());
    panel.add(repaintFrequencyTextField, GridBagHelper.rightCol());

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

    zRangeLabel = new JLabel("Z range [from:step:to]:");
    panel.add(zRangeLabel, GridBagHelper.leftCol());
    zRangeTextField = new JTextField("-50:10:50", 20);
    zRangeLabel.setEnabled(threeD);
    zRangeTextField.setEnabled(threeD);
    panel.add(zRangeTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    threeD = threeDCheckBox.isSelected();
    if (threeD) {
      parseRange(zRangeTextField.getText());
    }
    resolution = Double.parseDouble(resolutionTextField.getText());
    repaintFrequency = Integer.parseInt(repaintFrequencyTextField.getText());

  }

  @Override
  public void recordOptions() {
    if (threeD) {
      Recorder.recordOption("zRange", zFrom + ":" + zStep + ":" + zTo);
    }
    if (resolution != DEFAULT_RESOLUTION) {
      Recorder.recordOption("resolution", Double.toString(resolution));
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
      parseRange(rangeText);
    }
    resolution = Double.parseDouble(Macro.getValue(options, "resolution", "" + DEFAULT_RESOLUTION));
    repaintFrequency = Integer.parseInt(Macro.getValue(options, "repaintFrequency", "" + DEFAULT_REPAINT_FREQUENCY));
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {
    return getMethod();
  }

  protected abstract IncrementalRenderingMethod getMethod();

  private void parseRange(String rangeText) throws RuntimeException {
    Matcher m = Pattern.compile("^([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?):([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?):([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)$").matcher(rangeText);
    if (m.lookingAt()) {
      zFrom = Double.parseDouble(m.group(1));
      zStep = Double.parseDouble(m.group(2));
      zTo = Double.parseDouble(m.group(3));
      int nSlices = (int) ((zTo - zFrom) / zStep);
      if (zFrom > zTo) {
        throw new RuntimeException("Z range \"from\" value (" + zFrom + ") must be smaller than \"to\" value (" + zTo + ").");
      }
      if (nSlices < 1) {
        throw new RuntimeException("Invalid range: Must have at least one slice.");
      }
      zTo = nSlices * zStep + zFrom;
    } else {
      throw new RuntimeException("Wrong format of range field.");
    }
  }
}
