package cz.cuni.lf1.lge.ThunderSTORM.rendering;

/**
 * Simple rendering using scatter plot. If there is any molecule at the pixel
 * location, the pixel value will be a constant positive number and zero
 * otherwise.
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class ScatterRendering extends AbstractRendering implements IncrementalRenderingMethod {

  private ScatterRendering(Builder builder) {
    super(builder);
  }

  public static class Builder extends AbstractBuilder<Builder, ScatterRendering> {

    public ScatterRendering build() {
      super.validate();
      return new ScatterRendering(this);
    }
  }

  protected void drawPoint(double x, double y, double z, double dx) {
    if (isInBounds(x, y)) {
      int u = (int) Math.round((x - xmin) / resolution);
      int v = (int) Math.round((y - ymin) / resolution);
      image.setf(u, v, Float.MAX_VALUE);
    }
  }
}
