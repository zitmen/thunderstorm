package cz.cuni.lf1.lge.ThunderSTORM.rendering;

/**
 * Rendering using Averaged shifted histogram.
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class ASHRendering extends AbstractRendering implements IncrementalRenderingMethod {

  protected int shifts;

  protected ASHRendering(Builder builder) {
    super(builder);
    this.shifts = builder.shifts;
  }

  public static class Builder extends AbstractBuilder<Builder, ASHRendering> {

    protected int shifts = 2;

    /**
     * Number of shifts in one dimension. When one shift is set, the result is
     * the same as of HistogramRendering.
     *
     * @param shifts number of shifts, must be > 0
     */
    public Builder shifts(int shifts) {
      if (shifts <= 0) {
        throw new IllegalArgumentException("\"shifts\" argument must be positive integer. Passed value: " + shifts);
      }
      this.shifts = shifts;
      return this;
    }

    public ASHRendering build() {
      super.validate();
      return new ASHRendering(this);
    }
  }

  protected void drawPoint(double x, double y, double z, double dx) {
    if (isInBounds(x, y)) {
      int u = (int) Math.round(x / resolution);
      int v = (int) Math.round(y / resolution);

      for (int i = -shifts + 1; i < shifts; i++) {
        for (int j = -shifts + 1; j < shifts; j++) {
          if (u + i < imSizeX && u + i >= 0 && v + j < imSizeY && v + j >= 0) {
            image.setf(u + i, v + j, image.getf(u + i, v + j) + (shifts - Math.abs(i)) * (shifts - Math.abs(j)));
          }
        }
      }
    }
  }
}
