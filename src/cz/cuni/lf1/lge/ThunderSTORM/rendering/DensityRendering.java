package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import ij.process.ImageProcessor;
import static java.lang.Math.ceil;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF.erf;

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
            if(radius <= 0) {
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
        final int MAXRADIUS = (int) (defaultDX / resolution * 5);
        if(isInBounds(x, y)) {
            x = (x - xmin) / resolution;
            y = (y - ymin) / resolution;
            dx = dx / resolution;
            int u = (int) x;
            int v = (int) y;
            int actualRadius = (this.radius < 0) ? (int) Math.min(ceil(dx * 3), MAXRADIUS) : this.radius;
            double sqrt2dx = Math.sqrt(2) * dx;

            int w = threeDimensions ? ((int) ((z - zFrom) / zStep)) : 0;
            int affectedImages = Math.max((int) (3 * defaultDZ / zStep), 1);
            for(int idz = w - affectedImages; idz <= w + affectedImages; idz++) {
                if(idz >= 0 && idz < zSlices) {
                    double zerfdif;
                    if(threeDimensions) {
                        double aerf = (z - zFrom) - (idz - 1) * zStep;
                        double berf = (z - zFrom) - idz * zStep;
                        zerfdif = (-erf(berf / (Math.sqrt(2) * defaultDZ)) + erf(aerf / (Math.sqrt(2) * defaultDZ)));
                    } else {
                        zerfdif = 2;
                    }

                    for(int idx = u - actualRadius; idx <= u + actualRadius; idx++) {
                        if(idx >= 0 && idx < imSizeX) {
                            double difx = idx - x;
                            double xerfdif = (erf((difx) / sqrt2dx) - erf((difx + 1) / sqrt2dx));

                            for(int idy = v - actualRadius; idy <= v + actualRadius; idy++) {
                                if(idy >= 0 && idy < imSizeY) {
                                    double dify = idy - y;

                                    //3D gaussian blob integrated in z 
                                    //mathematica function for definite integral:
                                    //Integrate[(1/Sqrt[(2 Pi)^3 *s1^2* s2^2* s3^2]) * E^(-1/2*((x - x0)^2/(s1^2) + (y - y0)^2/(s2^2) + (z - z0)^2/(s3^2))), {z, a, b}]



                                    double val = 0.125
                                            * xerfdif
                                            * (erf((dify) / sqrt2dx) - erf((dify + 1) / sqrt2dx))
                                            * zerfdif;
                                    ImageProcessor img = slices[idz];
                                    img.setf(idx, idy, (float) val + img.getf(idx, idy));

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
