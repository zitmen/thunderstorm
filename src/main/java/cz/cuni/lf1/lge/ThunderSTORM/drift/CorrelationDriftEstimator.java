package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.RadialSymmetryFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingMethod;
import ij.process.FHT;
import ij.process.FloatProcessor;
import java.awt.geom.Point2D;
import java.util.Arrays;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.util.MathArrays;

/**
 *
 */
public class CorrelationDriftEstimator {

    /**
     *
     * @param x [px]
     * @param y [px]
     * @param frame
     * @param steps
     * @param roiWidth - [px] width of the original image or -1 for max(x)
     * @param roiHeight - [px] height of the original image or -1 for max(y)
     */
    public static CrossCorrelationDriftResults estimateDriftFromCoords(
            double[] x, double[] y, double[] frame,
            final int steps,
            final double magnification,
            int roiWidth, int roiHeight,
            boolean saveCorrelationImages) {

        final BinningResults bins = binResultByFrame(x, y, frame, steps);
        GUI.checkIJEscapePressed();
        int imageWidth = (roiWidth < 1) ? (int) MathProxy.ceil(VectorMath.max(x)) : roiWidth;
        int imageHeight = (roiHeight < 1) ? (int) MathProxy.ceil(VectorMath.max(y)) : roiHeight;

        RenderingMethod renderer = new ASHRendering.Builder().roi(0, imageWidth, 0, imageHeight).resolution(1 / magnification).shifts(2).build();
        RenderingMethod lowResRenderer = new ASHRendering.Builder().roi(0, imageWidth, 0, imageHeight).resolution(1).shifts(2).build();

        FloatProcessor firstImage = (FloatProcessor) renderer.getRenderedImage(bins.xBinnedByFrame[0], bins.yBinnedByFrame[0], null, null, null).getProcessor().convertToFloat();
        int paddedSize = MathProxy.nextPowerOf2(MathProxy.max(firstImage.getWidth(), firstImage.getHeight()));
        FHT firstImageFFT = createPaddedFFTImage(firstImage, paddedSize);

        FloatProcessor lowResFirstImage = (FloatProcessor) lowResRenderer.getRenderedImage(bins.xBinnedByFrame[0], bins.yBinnedByFrame[0], null, null, null).getProcessor().convertToFloat();
        int lowResPaddedSize = MathProxy.nextPowerOf2(MathProxy.max(lowResFirstImage.getWidth(), lowResFirstImage.getHeight()));
        FHT lowResFirstImageFFT = createPaddedFFTImage(lowResFirstImage, lowResPaddedSize);

        ImageStack correlationImages = null;
        if(saveCorrelationImages) {
            correlationImages = new ImageStack(paddedSize, paddedSize);
        }

        double[] driftXofImage = new double[steps];
        double[] driftYofImage = new double[steps];
        driftXofImage[0] = 0;
        driftYofImage[0] = 0;

        for(int i = 1; i < steps; i++) {
            IJ.showProgress((double) i / (double) (steps - 1));
            IJ.showStatus("Processing part " + i + " from " + (steps - 1) + "...");
            GUI.checkIJEscapePressed();

            FloatProcessor nextImage = (FloatProcessor) renderer.getRenderedImage(bins.xBinnedByFrame[i], bins.yBinnedByFrame[i], null, null, null).getProcessor().convertToFloat();
            FHT imageFFT = createPaddedFFTImage(nextImage, paddedSize);
            FHT crossCorrelationImage = computeCrossCorrelationImage(firstImageFFT, imageFFT);

            FloatProcessor lowResNextImage = (FloatProcessor) lowResRenderer.getRenderedImage(bins.xBinnedByFrame[i], bins.yBinnedByFrame[i], null, null, null).getProcessor().convertToFloat();
            FHT lowResImageFFT = createPaddedFFTImage(lowResNextImage, lowResPaddedSize);
            FHT lowResCrossCorrelationImage = computeCrossCorrelationImage(lowResFirstImageFFT, lowResImageFFT);

            if(saveCorrelationImages) {
                correlationImages.addSlice("", crossCorrelationImage);
            }

            //find maxima in low res image
            multiplyImageByGaussianMask(new Point2D.Double(driftXofImage[i - 1] + lowResPaddedSize / 2, driftXofImage[i - 1] + lowResPaddedSize / 2), lowResPaddedSize, lowResCrossCorrelationImage);
            Point2D.Double lowResMaximumCoords = CorrelationDriftEstimator.findMaxima(lowResCrossCorrelationImage);
            lowResMaximumCoords = CorrelationDriftEstimator.findMaximaWithSubpixelPrecision(lowResMaximumCoords, 11, lowResCrossCorrelationImage);

            //translate maxima coords from low res image to high res image
            Point2D.Double highResMaximumCoords = new Point2D.Double(
                    crossCorrelationImage.getWidth() / 2 + magnification * (lowResMaximumCoords.x - (lowResCrossCorrelationImage.getWidth() / 2)),
                    crossCorrelationImage.getHeight() / 2 + magnification * (lowResMaximumCoords.y - (lowResCrossCorrelationImage.getHeight() / 2)));
            //find maxima in high res image
            highResMaximumCoords = CorrelationDriftEstimator.findMaximaWithSubpixelPrecision(highResMaximumCoords, 11, crossCorrelationImage);

            driftXofImage[i] = (crossCorrelationImage.getWidth() / 2 - highResMaximumCoords.x);
            driftYofImage[i] = (crossCorrelationImage.getHeight() / 2 - highResMaximumCoords.y);
        }

        //scale
        for(int i = 0; i < driftXofImage.length; i++) {
            driftXofImage[i] = driftXofImage[i] / magnification;
            driftYofImage[i] = driftYofImage[i] / magnification;
        }

        //interpolate the drift using loess interpolator, or linear interpolation if not enough data for loess
        PolynomialSplineFunction xFunction;
        PolynomialSplineFunction yFunction;
        if(steps < 4) {
            LinearInterpolator interpolator = new LinearInterpolator();
            xFunction = addLinearExtrapolationToBorders(interpolator.interpolate(bins.binCenters, driftXofImage), bins.minFrame, bins.maxFrame);
            yFunction = addLinearExtrapolationToBorders(interpolator.interpolate(bins.binCenters, driftYofImage), bins.minFrame, bins.maxFrame);
        } else {
            LoessInterpolator interpolator = new LoessInterpolator(0.5, 0);
            xFunction = addLinearExtrapolationToBorders(interpolator.interpolate(bins.binCenters, driftXofImage), bins.minFrame, bins.maxFrame);
            yFunction = addLinearExtrapolationToBorders(interpolator.interpolate(bins.binCenters, driftYofImage), bins.minFrame, bins.maxFrame);
        }

        IJ.showStatus("");
        IJ.showProgress(1.0);
        return new CrossCorrelationDriftResults(correlationImages, xFunction, yFunction, bins.binCenters, driftXofImage, driftYofImage, 1 / magnification, bins.minFrame, bins.maxFrame, MoleculeDescriptor.Units.PIXEL);
    }

    private static FHT createPaddedFFTImage(FloatProcessor nextImage, int paddedSize) {
        FHT imageFFT = new FHT(Padding.padToBiggerSquare(nextImage, Padding.PADDING_ZERO, paddedSize));
        imageFFT.setShowProgress(false);
        imageFFT.transform();
        return imageFFT;
    }

    private static FHT computeCrossCorrelationImage(FHT image1FFT, FHT image2FFT) {
        FHT crossCorrelationImage = image1FFT.conjugateMultiply(image2FFT);
        crossCorrelationImage.setShowProgress(false);
        crossCorrelationImage.inverseTransform();
        crossCorrelationImage.swapQuadrants();
        return crossCorrelationImage;
    }

    private static void multiplyImageByGaussianMask(Point2D.Double gaussianCenter, double gaussianSigma, FloatProcessor image) {
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                double maskValue = MathProxy.exp(-(MathProxy.sqr(x - gaussianCenter.x) + MathProxy.sqr(y - gaussianCenter.y)) / (2 * gaussianSigma * gaussianSigma));
                float newValue = (float) (image.getf(x, y) * maskValue);
                image.setf(x, y, newValue);
            }
        }
    }

    private static class BinningResults {

        double[][] xBinnedByFrame;
        double[][] yBinnedByFrame;
        double[] binCenters;
        int minFrame;
        int maxFrame;

        public BinningResults(double[][] xBinnedByFrame, double[][] yBinnedByFrame, double[] binCenters, int minFrame, int maxFrame) {
            this.xBinnedByFrame = xBinnedByFrame;
            this.yBinnedByFrame = yBinnedByFrame;
            this.binCenters = binCenters;
            this.minFrame = minFrame;
            this.maxFrame = maxFrame;
        }
    }

    private static BinningResults binResultByFrame(double[] x, double[] y, double[] frame, int binCount) {
        double minFrame = findMinFrame(frame);
        double maxFrame = findMaxFrame(frame);

        if(maxFrame == minFrame) {
            throw new RuntimeException("Requires multiple frames.");
        }

        MathArrays.sortInPlace(frame, x, y);
        int detectionsPerBin = frame.length / binCount;

        //alloc space for binned results
        double[][] xBinnedByFrame = new double[binCount][];
        double[][] yBinnedByFrame = new double[binCount][];
        double[] binCenters = new double[binCount];
        int currentPos = 0;
        for(int i = 0; i < binCount; i++) {
            int endPos = currentPos + detectionsPerBin;
            if(endPos >= frame.length || i == binCount - 1) {
                endPos = frame.length;
            } else {
                double frameAtEndPos = frame[endPos - 1];
                while(endPos < frame.length - 1 && frame[endPos] == frameAtEndPos) {
                    endPos++;
                }
            }
            if(currentPos > frame.length - 1) {
                xBinnedByFrame[i] = new double[0];
                yBinnedByFrame[i] = new double[0];
                binCenters[i] = maxFrame;
            } else {
                xBinnedByFrame[i] = Arrays.copyOfRange(x, currentPos, endPos);
                yBinnedByFrame[i] = Arrays.copyOfRange(y, currentPos, endPos);
                binCenters[i] = (frame[currentPos] + frame[endPos - 1]) / 2;
            }
            currentPos = endPos;
        }
        return new BinningResults(xBinnedByFrame, yBinnedByFrame, binCenters, (int) minFrame, (int) maxFrame);
    }

    private static double findMinFrame(double[] frame) {
        //find min and max frame
        double minFrame = frame[0];
        for(int i = 0; i < frame.length; i++) {
            if(frame[i] < minFrame) {
                minFrame = frame[i];
            }
        }
        return minFrame;
    }

    private static double findMaxFrame(double[] frame) {
        //find min and max frame
        double maxFrame = frame[0];
        for(int i = 0; i < frame.length; i++) {
            if(frame[i] > maxFrame) {
                maxFrame = frame[i];
            }
        }
        return maxFrame;
    }


    static Point2D.Double findMaxima(FloatProcessor crossCorrelationImage) {
        float[] pixels = (float[]) crossCorrelationImage.getPixels();
        int maxIndex = 0;
        float max = pixels[0];
        for(int i = 0; i < pixels.length; i++) {
            if(pixels[i] > max) {
                max = pixels[i];
                maxIndex = i;
            }
        }
        return new Point2D.Double(maxIndex % crossCorrelationImage.getWidth(), maxIndex / crossCorrelationImage.getWidth());
    }

    static Point2D.Double findMaximaWithSubpixelPrecision(Point2D.Double maximumCoords, int roiSize, FHT crossCorrelationImage) {
        double[] subImageData = new double[roiSize * roiSize];
        float[] pixels = (float[]) crossCorrelationImage.getPixels();
        int roiX = (int) maximumCoords.x - (roiSize - 1) / 2;
        int roiY = (int) maximumCoords.y - (roiSize - 1) / 2;

        if(isCloseToBorder((int) maximumCoords.x, (int) maximumCoords.y, (roiSize - 1) / 2, crossCorrelationImage)) {
            return maximumCoords;
        }

        for(int ys = roiY; ys < roiY + roiSize; ys++) {
            int offset1 = (ys - roiY) * roiSize;
            int offset2 = ys * crossCorrelationImage.getWidth() + roiX;
            for(int xs = 0; xs < roiSize; xs++) {
                subImageData[offset1++] = pixels[offset2++];
            }
        }

        SubImage subImage = new SubImage(roiSize, roiSize, null, null, subImageData, 0, 0);
        RadialSymmetryFitter radialSymmetryFitter = new RadialSymmetryFitter();
        Molecule psf = radialSymmetryFitter.fit(subImage);

        return new Point2D.Double((int) maximumCoords.x + psf.getX(), (int) maximumCoords.y + psf.getY());
    }

    //
    public static PolynomialSplineFunction addLinearExtrapolationToBorders(PolynomialSplineFunction spline, int minFrame, int maxFrame) {
        PolynomialFunction[] polynomials = spline.getPolynomials();
        double[] knots = spline.getKnots();

        boolean addToBeginning = knots[0] != minFrame;
        boolean addToEnd = knots[knots.length - 1] != maxFrame;
        int sizeIncrease = 0 + (addToBeginning ? 1 : 0) + (addToEnd ? 1 : 0);
        if(!addToBeginning && !addToEnd) {
            return spline; //do nothing
        }

        //construct new knots and polynomial arrays
        double[] newKnots = new double[knots.length + sizeIncrease];
        PolynomialFunction[] newPolynomials = new PolynomialFunction[polynomials.length + sizeIncrease];
        //add to beginning
        if(addToBeginning) {
            //add knot
            newKnots[0] = minFrame;
            System.arraycopy(knots, 0, newKnots, 1, knots.length);
            //add function
            double derivativeAtFirstKnot = polynomials[0].derivative().value(0);
            double valueAtFirstKnot = spline.value(knots[0]);
            PolynomialFunction beginningFunction = new PolynomialFunction(new double[]{valueAtFirstKnot - (knots[0] - minFrame) * derivativeAtFirstKnot, derivativeAtFirstKnot});
            newPolynomials[0] = beginningFunction;
            System.arraycopy(polynomials, 0, newPolynomials, 1, polynomials.length);
        } else {
            System.arraycopy(knots, 0, newKnots, 0, knots.length);
            System.arraycopy(polynomials, 0, newPolynomials, 0, polynomials.length);
        }
        //add to end
        if(addToEnd) {
            //add knot
            newKnots[newKnots.length - 1] = maxFrame;
            //add function
            double derivativeAtLastKnot = polynomials[polynomials.length - 1].polynomialDerivative().value(knots[knots.length - 1] - knots[knots.length - 2]);
            double valueAtLastKnot = spline.value(knots[knots.length - 1]);
            PolynomialFunction endFunction = new PolynomialFunction(new double[]{valueAtLastKnot, derivativeAtLastKnot});
            newPolynomials[newPolynomials.length - 1] = endFunction;
        }

        return new PolynomialSplineFunction(newKnots, newPolynomials);

    }

    static boolean isCloseToBorder(int x, int y, int subimageSize, ImageProcessor image) {
        if(x < subimageSize || x > image.getWidth() - subimageSize - 1) {
            return true;
        }
        if(y < subimageSize || y > image.getHeight() - subimageSize - 1) {
            return true;
        }
        return false;
    }
}
