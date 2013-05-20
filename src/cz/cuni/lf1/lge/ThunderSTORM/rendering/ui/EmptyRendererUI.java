package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IRenderer;
import javax.swing.JPanel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class EmptyRendererUI implements IRendererUI {

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
  public void setSize(int width, int height) {
  }

  @Override
  public IRenderer getImplementation() {
    return new IRenderer() {
      @Override
      public void renderLater(double[] x, double[] y, double dx) {
      }

      @Override
      public void renderLater(double[] x, double[] y, double[] dx) {
      }

      @Override
      public void repaintLater() {
      }
    };
  }
}
