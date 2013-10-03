package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.ASHRenderingUI;
import ij.process.ImageProcessor;

/**
 * Rendering using Averaged shifted histogram.
 *
 */
public class ASHRendering extends AbstractRendering implements IncrementalRenderingMethod {

  protected int shifts;
  protected int zShifts;

  protected ASHRendering(Builder builder) {
    super(builder);
    this.shifts = builder.shifts;
    this.zShifts = threeDimensions ? builder.zShifts : 1;
  }

    @Override
    public String getRendererName() {
        return ASHRenderingUI.name;
    }

  public static class Builder extends AbstractBuilder<Builder, ASHRendering> {

    protected int shifts = 2;
    protected int zShifts = 1;

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

    public Builder zShifts(int zShifts) {
      if (zShifts <= 0) {
        throw new IllegalArgumentException("\"zShifts\" argument must be positive integer. Passed value: " + shifts);
      }
      this.zShifts = zShifts;
      return this;
    }

    @Override
    public ASHRendering build() {
      super.validate();
      return new ASHRendering(this);
    }
  }

  @Override
  protected void drawPoint(double x, double y, double z, double dx) {
    if (isInBounds(x, y)) {
      int u = (int) ((x - xmin) / resolution);
      int v = (int) ((y - ymin) / resolution);
      int w = threeDimensions ? ((int) ((z - zFrom) / zStep)) : 0;

      for (int k = -zShifts + 1; k < zShifts; k++) {
        if (w + k < zSlices && w + k >= 0) {

          ImageProcessor img = slices[w + k];
          for (int i = -shifts + 1; i < shifts; i++) {

            for (int j = -shifts + 1; j < shifts; j++) {
              if (u + i < imSizeX && u + i >= 0
                      && v + j < imSizeY && v + j >= 0) {
                img.setf(u + i, v + j, img.getf(u + i, v + j) + (shifts - Math.abs(i)) * (shifts - Math.abs(j)) * (zShifts - Math.abs(k)));
              }
            }
          }
        }
      }
    }
  }
}
