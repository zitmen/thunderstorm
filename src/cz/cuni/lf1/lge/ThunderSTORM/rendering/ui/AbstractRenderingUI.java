package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public abstract class AbstractRenderingUI implements IRendererUI {

  double resolution;
  int sizeX;
  int sizeY;
  int repaintFrequency;
  JTextField resolutionTextField, repaintFrequencyTextField;
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
    panel.add(new JLabel("Resolution: "), GridBagHelper.leftCol());
    panel.add(resolutionTextField, GridBagHelper.rightCol());
    panel.add(new JLabel("Repaint frequency: "), GridBagHelper.leftCol());
    panel.add(repaintFrequencyTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    resolution = Double.parseDouble(resolutionTextField.getText());
    repaintFrequency = Integer.parseInt(repaintFrequencyTextField.getText());
  }

  @Override
  public void recordOptions() {
    if (resolution != DEFAULT_RESOLUTION) {
      Recorder.recordOption("resolution", Double.toString(resolution));
    }
    if (repaintFrequency != DEFAULT_REPAINT_FREQUENCY) {
      Recorder.recordOption("repaintFrequency", Integer.toString(repaintFrequency));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    resolution = Double.parseDouble(Macro.getValue(options, "resolution", "" + DEFAULT_RESOLUTION));
    repaintFrequency = Integer.parseInt(Macro.getValue(options, "repaintFrequency", "" + DEFAULT_REPAINT_FREQUENCY));
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {
    return getMethod();
  }

  protected abstract IncrementalRenderingMethod getMethod();
}
