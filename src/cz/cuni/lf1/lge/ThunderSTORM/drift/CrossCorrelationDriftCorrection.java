package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import ij.process.FHT;
import ij.process.FloatProcessor;
import java.awt.geom.Point2D;
import java.util.Arrays;
import cz.cuni.lf1.lge.ThunderSTORM.util.Math;
import ij.IJ;
import ij.gui.Plot;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 *
 */
public class CrossCorrelationDriftCorrection {

  int imageWidth = 100;
  int imageHeight = 100;
  double magnification = 5;
  int steps;
  double[] x;
  double[] y;
  double[] frame;
  double[][] xBinnedByFrame; //xBinnedByFrame[bin]
  double[][] yBinnedByFrame;
  double minFrame, maxFrame;
  double[] binDriftX;
  double[] binDriftY;
  double[] binCenters;
  PolynomialSplineFunction xFunction;
  PolynomialSplineFunction yFunction;

  /**
   *
   * @param x
   * @param y
   * @param frame
   * @param steps
   * @param renderingMagnification
   * @param roiWidth - width of the original image or -1 for max(x)
   * @param roiHeight - height of the original image or -1 for max(y)
   */
  public CrossCorrelationDriftCorrection(double[] x, double[] y, double[] frame, int steps, double renderingMagnification, int roiWidth, int roiHeight) {
    this.x = x;
    this.y = y;
    this.frame = frame;
    this.steps = steps;
    this.magnification = renderingMagnification;
    if (roiWidth < 1) {
      this.imageWidth = (int) Math.ceil(Math.max(x));
    }
    if (roiHeight < 1) {
      this.imageHeight = (int) Math.ceil(Math.max(y));
    }
    run();
  }

  private void run() {
    int paddedSize = nextPowerOf2(Math.max((int) (imageWidth * magnification), (int) (imageHeight * magnification)));
    int originalImageSize = Math.max(imageWidth, imageHeight);
    double minRoi = 0 - ((double) paddedSize / magnification - originalImageSize) / 2;
    double maxRoi = originalImageSize + ((double) paddedSize / magnification - originalImageSize) / 2;
//    double ymin = 0 - ((double) paddedSize / magnification - imageHeight) / 2;
//    double ymax = imageHeight + ((double) paddedSize / magnification - imageHeight) / 2;

    binResultByFrame();

    binDriftX = new double[steps];
    binDriftY = new double[steps];
    binDriftX[0] = 0;   //first image has no drift
    binDriftY[0] = 0;

    HistogramRendering renderer = new HistogramRendering.Builder().imageSize(paddedSize, paddedSize).roi(minRoi, maxRoi, minRoi, maxRoi).build();
    FHT firstImage = new FHT(renderer.getRenderedImage(xBinnedByFrame[0], yBinnedByFrame[0], null, null).getProcessor());
    firstImage.setShowProgress(false);
    firstImage.transform();
    FHT secondImage;
    for (int i = 1; i < xBinnedByFrame.length; i++) {
      IJ.showStatus("processing part " + i + " from " + steps);
      secondImage = new FHT(renderer.getRenderedImage(xBinnedByFrame[i], yBinnedByFrame[i], null, null).getProcessor());
      secondImage.setShowProgress(false);
      //new ImagePlus("render " + i,renderer.getRenderedImage(xBinnedByFrame[i], yBinnedByFrame[i], null, null).getProcessor()).show();
      secondImage.transform();

      FHT crossCorrelationImage = firstImage.conjugateMultiply(secondImage);
      crossCorrelationImage.setShowProgress(false);
      crossCorrelationImage.inverseTransform();
      crossCorrelationImage.swapQuadrants();

      //new ImagePlus("crossCorrelation " + i, crossCorrelationImage.duplicate()).show();
      //find maxima
      Point2D.Double maximumCoords = findMaxima(crossCorrelationImage);
      binDriftX[i] = (crossCorrelationImage.getWidth() / 2 - maximumCoords.x) / magnification;
      binDriftY[i] = (crossCorrelationImage.getHeight() / 2 - maximumCoords.y) / magnification;
    }

    //cumulative sum to get offset from the first bin
    //cumulativeSum(binDriftX);
    //cumulativeSum(binDriftY);

    //interpolate the 
    SplineInterpolator interpolator = new SplineInterpolator();
    xFunction = addLinearExtrapolationToBorders(interpolator.interpolate(binCenters, binDriftX));
    yFunction = addLinearExtrapolationToBorders(interpolator.interpolate(binCenters, binDriftY));

    x = null;
    y = null;
    frame = null;
    xBinnedByFrame = null;
    yBinnedByFrame = null;
  }

  void binResultByFrame() {
    //find min and max frame
    minFrame = frame[0];
    maxFrame = frame[0];
    for (int i = 0; i < frame.length; i++) {
      if (frame[i] < minFrame) {
        minFrame = frame[i];
      }
      if (frame[i] > maxFrame) {
        maxFrame = frame[i];
      }
    }

    //alloc space for binned results
    //  count results in each bin
    int stepFrame = (int) Math.ceil((maxFrame - minFrame) / steps);
    xBinnedByFrame = new double[steps][];
    yBinnedByFrame = new double[steps][];
    int[] binCounts = new int[steps];
    Arrays.fill(binCounts, 0, binCounts.length, 0);
    for (int i = 0; i < frame.length; i++) {
      int bin;
      if (frame[i] == maxFrame) {
        bin = steps - 1;
      } else {
        bin = (int) ((frame[i] - minFrame) / stepFrame);
      }
      binCounts[bin]++;
    }
    //  alloc 
    for (int i = 0; i < xBinnedByFrame.length; i++) {
      xBinnedByFrame[i] = new double[binCounts[i]];
      yBinnedByFrame[i] = new double[binCounts[i]];
    }

    //fill in values (in reversed order for simplicity)
    for (int i = 0; i < frame.length; i++) {
      int bin;
      if (frame[i] == maxFrame) {
        bin = steps - 1;
      } else {
        bin = (int) ((frame[i] - minFrame) / stepFrame);
      }
      xBinnedByFrame[bin][binCounts[bin] - 1] = x[i];
      yBinnedByFrame[bin][binCounts[bin] - 1] = y[i];
      binCounts[bin]--;
    }
    //save bin centers
    binCenters = new double[steps];
    for (int i = 0; i < steps; i++) {
      binCenters[i] = minFrame + stepFrame / 2 + i * stepFrame;
    }

  }

  private static int nextPowerOf2(int num) {
    int powof2 = 1;
    while (powof2 < num) {
      powof2 <<= 1;
    }
    return powof2;
  }

  private static Point2D.Double findMaxima(FloatProcessor crossCorrelationImage) {
    float[] pixels = (float[]) crossCorrelationImage.getPixels();
    int maxIndex = 0;
    float max = pixels[0];
    for (int i = 0; i < pixels.length; i++) {
      if (pixels[i] > max) {
        max = pixels[i];
        maxIndex = i;
      }
    }
    return new Point2D.Double(maxIndex % crossCorrelationImage.getWidth(), maxIndex / crossCorrelationImage.getWidth());

  }

  private static void cumulativeSum(double[] binDrift) {
    if (binDrift.length > 0) {
      double sum = binDrift[0];
      for (int i = 1; i < binDrift.length; i++) {
        binDrift[i] += sum;
        sum = binDrift[i];
      }
    }
  }

  public Point2D.Double getFrameOffset(int frameNumber) {
    return getFrameOffset((double) frameNumber);
  }

  public Point2D.Double getFrameOffset(double frameNumber) {
//    if (frameNumber < binCenters[0]) {
//      return new Point2D.Double(0, 0);
//    }
//    if (frameNumber > binCenters[binCenters.length - 1]) {
//      return new Point2D.Double(binDriftX[binDriftX.length - 1], binDriftY[binDriftY.length - 1]);
//    }
    return new Point2D.Double(xFunction.value(frameNumber), yFunction.value(frameNumber));
  }

  //
  private PolynomialSplineFunction addLinearExtrapolationToBorders(PolynomialSplineFunction spline) {
    PolynomialFunction[] polynomials = spline.getPolynomials();
    double[] knots = spline.getKnots();
    double derivativeAtFirstKnot = polynomials[0].derivative().value(knots[0]);
    double derivativeAtLastKnot = polynomials[polynomials.length - 1].polynomialDerivative().value(knots[knots.length - 1] - knots[knots.length - 2]);
    double valueAtFirstKnot = spline.value(knots[0]);
    double valueAtLastKnot = spline.value(knots[knots.length - 1]);
    PolynomialFunction beginningFunction = new PolynomialFunction(new double[]{valueAtFirstKnot - derivativeAtFirstKnot*knots[0], derivativeAtFirstKnot});
    PolynomialFunction endFunction = new PolynomialFunction(new double[]{valueAtLastKnot, derivativeAtLastKnot});

    //construct new knot array
    double[] newKnots = new double[knots.length + 2];
    newKnots[0] = minFrame;
    System.arraycopy(knots, 0, newKnots, 1, knots.length);
    newKnots[newKnots.length - 1] = maxFrame;
    //construct new polynomial array
    PolynomialFunction[] newPolynomials = new PolynomialFunction[polynomials.length + 2];
    newPolynomials[0] = beginningFunction;
    System.arraycopy(polynomials, 0, newPolynomials, 1, polynomials.length);
    newPolynomials[newPolynomials.length - 1] = endFunction;

    return new PolynomialSplineFunction(newKnots, newPolynomials);

  }

  static void plotFunction(String caption, UnivariateFunction function, double from, double to, double step) {
    double[] x = new double[(int) java.lang.Math.floor((to - from) / step)];
    double[] y = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      x[i] = from + i * step;
      y[i] = function.value(x[i]);
    }
    Plot plot = new Plot(caption, "x", "y", x, y);
    plot.show();
  }
}
