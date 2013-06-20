package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class DensityRenderingUI extends AbstractRenderingUI {

  public DensityRenderingUI() {
  }

  public DensityRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "Density Renderer";
  }

  @Override
  public IncrementalRenderingMethod getMethod() {
    return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).build();
  }
}
