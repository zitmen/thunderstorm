package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.rendering.DensityRendering;
import cz.cuni.lf1.rendering.IncrementalRenderingMethod;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class DensityRenderingWrapper extends AbstractRenderingWrapper {

  public DensityRenderingWrapper(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "Density Renderer";
  }

  @Override
  protected IncrementalRenderingMethod getMethod() {
    return new DensityRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).build();
  }
}
