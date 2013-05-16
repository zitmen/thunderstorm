package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.rendering.QueuedRenderer;
import ij.IJ;
import ij.ImagePlus;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public abstract class AbstractRenderingWrapper implements IRenderer {

  protected QueuedRenderer renderer;    //must be set in subclass
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
      IJ.run(image, "Enhance Contrast", "saturated=0.05");
    }
  };

  public AbstractRenderingWrapper(int sizeX, int sizeY) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
  }

  @Override
  public void renderAsync(double[] x, double[] y, double[] dx) {
    renderer.renderLater(x, y, dx);
  }

  @Override
  public void renderAsync(double[] x, double[] y, double dx) {
    renderer.renderLater(x, y, dx);
  }

  @Override
  public void repaintAsync() {
    renderer.repaintLater();
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    resolutionTextField = new JTextField("0.2", 20);
    repaintFrequencyTextField = new JTextField("20", 20);
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

    IncrementalRenderingMethod method = getMethod();
    image = new ImagePlus(method.getClass().getSimpleName(), method.getRenderedImage());
    renderer = new QueuedRenderer(method, repaint, repaintFrequency);
  }

  protected abstract IncrementalRenderingMethod getMethod();
}
