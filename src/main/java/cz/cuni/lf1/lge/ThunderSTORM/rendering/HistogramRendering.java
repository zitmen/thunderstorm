package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.HistogramRenderingUI;
import ij.process.FloatProcessor;
import java.util.Random;

/**
 * Rendering using a two-dimensional histogram. When average parameter is set as
 * > 0, the result is the average of multiple histograms, each made from
 * molecule locations jittered according to the uncertainty of localization
 * (dx).
 */
public class HistogramRendering extends AbstractRendering implements IncrementalRenderingMethod {

    protected int avg;
    protected Random rnd = new Random();

    private HistogramRendering(Builder builder) {
        super(builder);
        this.avg = builder.avg;
    }

    @Override
    public String getRendererName() {
        return HistogramRenderingUI.name;
    }

    public static class Builder extends AbstractBuilder<Builder, HistogramRendering> {

        protected int avg = 0;

        /**
         * Sets how many jittered histograms are calculated and averaged to
         * create the final image.
         */
        public Builder average(int avg) {
            if(avg < 0) {
                throw new IllegalArgumentException("Average parameter must be positive integer. Passed value: " + avg);
            }
            this.avg = avg;
            return this;
        }

        @Override
        public HistogramRendering build() {
            super.validate();
            return new HistogramRendering(this);
        }
    }

    @Override
    protected void drawPoint(double x, double y, double z, double dx, double dz) {

        if(avg >= 1) {
            for(int i = 0; i < avg; i++) {
                double newX = x + rnd.nextGaussian() * dx;
                double newY = y + rnd.nextGaussian() * dx;
                double newZ = z + rnd.nextGaussian() * dz;
                if(isInBounds(newX, newY)) {
                    int u = (int) ((newX - xmin) / resolution);
                    int v = (int) ((newY - ymin) / resolution);
                    int w = threeDimensions ? ((int) ((newZ - zFrom) / zStep)) : 0;
                    if(w >= 0 && w < zSlices) {
                        FloatProcessor image = (FloatProcessor) slices[w];
                        image.setf(u, v, image.getf(u, v) + 1.0f / avg);
                    }
                }
            }
        } else {
            if(isInBounds(x, y)) {
                int u = (int) ((x - xmin) / resolution);
                int v = (int) ((y - ymin) / resolution);
                int w = threeDimensions ? ((int) ((z - zFrom) / zStep)) : 0;
                if(w >= 0 && w < zSlices) {
                    FloatProcessor img = (FloatProcessor) slices[w];
                    img.setf(u, v, img.getf(u, v) + 1);
                }
            }
        }
    }
}
