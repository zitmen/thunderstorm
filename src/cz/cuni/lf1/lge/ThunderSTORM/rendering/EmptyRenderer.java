package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import javax.swing.JPanel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class EmptyRenderer implements IRenderer {

  @Override
  public String getName() {
    return "No Renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    return null;
  }

  @Override
  public void readParameters() {
  }

  @Override
  public void renderAsync(double[] x, double[] y, double[] dx) {
  }

  @Override
  public void renderAsync(double[] x, double[] y, double dx) {
  }

  @Override
  public void repaintAsync() {
  }

  @Override
  public void setSize(int width, int height) {
  }
}
