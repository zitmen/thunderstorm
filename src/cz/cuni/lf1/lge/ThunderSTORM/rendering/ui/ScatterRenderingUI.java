package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.rendering.ScatterRendering;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class ScatterRenderingUI extends AbstractRenderingUI {

  public ScatterRenderingUI() {
  }

  public ScatterRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "Scatter Renderer";
  }

  @Override
  public IncrementalRenderingMethod getMethod() {
    return new ScatterRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).build();
  }
}
