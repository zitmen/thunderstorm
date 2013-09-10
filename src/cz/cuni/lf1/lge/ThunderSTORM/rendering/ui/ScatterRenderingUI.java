package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ScatterRendering;

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
    if (threeD) {
      return new ScatterRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1/magnification).zRange(zFrom, zTo, zStep).build();
    } else {
      return new ScatterRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(1/magnification).build();
    }
  }
}
