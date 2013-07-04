package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import ij.process.ImageProcessor;
import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.exp;
import org.apache.commons.math3.special.Erf;

/**
 * This rendering method draws a normalized gaussian blob at every molecule
 * location.
 */
public class DensityRendering extends AbstractRendering implements IncrementalRenderingMethod {

  protected int radius;

  private DensityRendering(Builder builder) {
    super(builder);
    this.radius = builder.radius;
  }

  public static class Builder extends AbstractBuilder<Builder, DensityRendering> {

    protected int radius = -1;

    /**
     * The radius around the rendered point where pixels are updated (in
     * pixels). When not set, the value 3*dx (in pixels)is used.
     *
     * @param pixels
     */
    public Builder radius(int pixels) {
      if (radius <= 0) {
        throw new IllegalArgumentException("Radius must be positive. Passed value = " + pixels);
      }
      this.radius = pixels;
      return this;
    }

    @Override
    public DensityRendering build() {
      super.validate();
      return new DensityRendering(this);
    }
  }

  @Override
  protected void drawPoint(double x, double y, double z, double dx) {

    if (isInBounds(x, y)) {
      x = (x - xmin) / resolution;
      y = (y - ymin) / resolution;
      dx = dx / resolution;
      int u = (int) x;
      int v = (int) y;
      int w = threeDimensions ? ((int) ((z - zFrom) / zStep)) : 0;
      int actualRadius = (this.radius < 0) ? (int) ceil(dx * 3) : this.radius;


      int affectedImages = (int) (3 * defaultDZ / zStep);

      for (int idz = w - affectedImages; idz <= w + affectedImages; idz++) {
        if (idz >= 0 && idz < zSlices) {

          for (int idx = u - actualRadius; idx < u + actualRadius; idx++) {
            if (idx >= 0 && idx < imSizeX) {

              for (int idy = v - actualRadius; idy < v + actualRadius; idy++) {
                if (idy >= 0 && idy < imSizeY) {

                  double squareDist = squareDist(idx, idy, x, y);
                  if (squareDist <= (actualRadius * actualRadius)) {
                    //3D gaussian blob integrated in z 
                    //mathematica function for definite integral:
                    //Integrate[(1/Sqrt[(2 Pi)^3 *s1^2* s2^2* s3^2]) * E^(-1/2*((x - x0)^2/(s1^2) + (y - y0)^2/(s2^2) + (z - z0)^2/(s3^2))), {z, a, b}]
                    double aerf = (z - zFrom) - (idz - 1) * zStep;
                    double berf = (z - zFrom) - idz * zStep;
                    double val = 1 / (2 * PI * dx * dx * defaultDZ) * exp(-0.5 * squareDist / (dx * dx)) * defaultDZ * Erf.erf(berf / (Math.sqrt(2) * defaultDZ), aerf / (Math.sqrt(2) * defaultDZ));
                    ImageProcessor image = slices[idz];
                    image.setf(idx, idy, (float) val + image.getf(idx, idy));
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private double squareDist(double x1, double y1, double x2, double y2) {
    return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
  }
}
