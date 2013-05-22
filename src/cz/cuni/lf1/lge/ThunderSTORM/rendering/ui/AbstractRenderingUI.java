package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IRenderer;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.rendering.IncrementalRenderingMethod;
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

  protected double resolution;
  protected int sizeX;
  protected int sizeY;
  protected int repaintFrequency;
  protected JTextField resolutionTextField, repaintFrequencyTextField;
  protected ImagePlus image;            //must be set in subclass
  protected Runnable repaint = new Runnable() {
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
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    resolutionTextField = new JTextField("" + DEFAULT_RESOLUTION, 20);
    repaintFrequencyTextField = new JTextField("" + DEFAULT_REPAINT_FREQUENCY, 20);
    panel.add(new JLabel("Resolution: "), GridBagHelper.pos(0, 0));
    panel.add(resolutionTextField, GridBagHelper.pos(1, 0));
    panel.add(new JLabel("Repaint frequency: "), GridBagHelper.pos(0, 1));
    panel.add(repaintFrequencyTextField, GridBagHelper.pos(1, 1));

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
  public IRenderer getImplementation() {
    IncrementalRenderingMethod method = getMethod();
    image = new ImagePlus(method.getClass().getSimpleName(), method.getRenderedImage());
    return new RenderingQueue(method, repaint, repaintFrequency);
  }

  protected abstract IncrementalRenderingMethod getMethod();
}
