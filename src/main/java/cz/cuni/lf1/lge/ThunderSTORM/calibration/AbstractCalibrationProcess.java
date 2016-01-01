package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.ICalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import java.awt.*;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.*;

abstract class AbstractCalibrationProcess implements ICalibrationProcess {

    // config
    protected static final int minimumFitsCount = 20;
    protected static final int polyFitMaxIters = 750;
    protected static final int finalPolyFitMaxIters = 2000;
    protected static final int minFitsInZRange = 3;
    protected static final int movingAverageLag = 5;
    protected static final boolean checkIfDefocusIsInRange = false;
    protected static final int inlierFittingMaxIters = 5;
    protected static final double inlierFittingInlierFraction = 0.9;
    protected static final boolean showResultsTable = false;

    // processing
    protected IFilterUI selectedFilterUI;
    protected IDetectorUI selectedDetectorUI;
    protected ICalibrationEstimatorUI calibrationEstimatorUI;
    protected DefocusFunction defocusModel;
    protected double stageStep;
    protected double zRange;

    // results
    protected double angle = 0.0;
    protected Homography.TransformationMatrix transformationMatrix = null;
    protected List<PSFSeparator.Position> usedPositions;
    protected DefocusFunction polynomS2Final;
    protected DefocusFunction polynomS1Final;
    protected ArrayList<DefocusFunction> allPolynomsS1;
    protected ArrayList<DefocusFunction> allPolynomsS2;
    protected double[] allFrames;
    protected double[] allSigma1s;
    protected double[] allSigma2s;

    public AbstractCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI, DefocusFunction defocusModel, double stageStep, double zRangeLimit) {
        this.selectedFilterUI = selectedFilterUI;
        this.selectedDetectorUI = selectedDetectorUI;
        this.calibrationEstimatorUI = calibrationEstimatorUI;
        this.defocusModel = defocusModel;
        this.stageStep = stageStep;
        this.zRange = zRangeLimit;
    }

    /**
     * Estimates the rotation angle of the cylindrical lens. If the lens is
     * aligned with camera or the angle is known, you can use setAngle(double)
     * instead.
     */
    protected double estimateAngle(ImagePlus imp, final Roi roi) {
        final List<Double> angles = Collections.synchronizedList(new ArrayList<Double>());
        final ImageStack stack = imp.getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        final ICalibrationEstimatorUI threadLocalEstimatorUI = calibrationEstimatorUI;
        Loop.withIndex(1, stack.getSize() + 1, new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi.getBounds());
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
                fp.setMask(roi.getMask());
                Thresholder.setCurrentImage(fp);
                List<Molecule> fits = threadLocalEstimatorUI.getThreadLocalImplementation().estimateParameters(fp,
                        Point.applyRoiMask(roi, selectedDetectorUI.getThreadLocalImplementation().detectMoleculeCandidates(selectedFilterUI.getThreadLocalImplementation().filterImage(fp))));
                framesProcessed.incrementAndGet();

                for (Molecule psf : fits) {
                    double s1 = psf.getParam(LABEL_SIGMA1);
                    double s2 = psf.getParam(LABEL_SIGMA2);
                    double ratio = s1 / s2;
                    if (ratio > 2 || ratio < 0.5) {
                        continue;
                    }
                    if (ratio < 1.2 && ratio > 0.83) {
                        continue;
                    }
                    angles.add(psf.getParam(LABEL_ANGLE));
                }
                IJ.showProgress(0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
                IJ.showStatus("Determining angle: frame " + framesProcessed + " of " + stack.getSize() + "...");
            }
        });
        // calculation of circular mean
        List<Double> sins = new ArrayList<Double>(angles);
        List<Double> coss = new ArrayList<Double>(angles);
        for(int i = 0; i < angles.size(); i++) {
            double fittedAngle = angles.get(i);
            //modulo 2PI/4
            fittedAngle = fittedAngle % (MathProxy.PI / 2);
            //modulo of a negative number is defined to be negative in java, so translate it to range [0,pi/2]
            if(fittedAngle < 0) {
                fittedAngle += MathProxy.PI / 2;
            }
            //*4
            fittedAngle *= 4;

            sins.set(i, MathProxy.sin(fittedAngle));
            coss.set(i, MathProxy.cos(fittedAngle));
        }
        double sin = bootstrapMeanEstimation(sins, 100, angles.size());
        double cos = bootstrapMeanEstimation(coss, 100, angles.size());
        return MathProxy.atan2(sin, cos) / 4;
    }

    protected void fitQuadraticPolynomials(Collection<PSFSeparator.Position> positions) {
        // fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
        IterativeFitting polynomialFitter = new IterativeFitting(inlierFittingMaxIters, inlierFittingInlierFraction);
        allPolynomsS1 = new ArrayList<DefocusFunction>();
        allPolynomsS2 = new ArrayList<DefocusFunction>();

        List<double[]> framesArrays = new ArrayList<double[]>();
        List<double[]> sigma1Arrays = new ArrayList<double[]>();
        List<double[]> sigma2Arrays = new ArrayList<double[]>();

        AtomicInteger moleculesProcessed = new AtomicInteger(0);
        usedPositions = new ArrayList<PSFSeparator.Position>();
        for(PSFSeparator.Position p : positions) {
            moleculesProcessed.incrementAndGet();
            IJ.showProgress(0.9 + 0.1 * (double) moleculesProcessed.intValue() / (double) positions.size());
            IJ.showStatus("Fitting polynoms: molecule " + moleculesProcessed + " of " + positions.size() + "...");

            try {
                if(p.getSize() < minimumFitsCount) {
                    continue;
                }
                double z0guess = guessZ0(p);
                p.discardFitsByFrameRange(z0guess - zRange/stageStep, z0guess + zRange/stageStep);

                // retrieve values again after filtering out fits not in range
                double[] framesArrayOfZ = p.getFramesAsArrayOfZ(z0guess, stageStep);
                double[] sigma1Array = p.getAsArray(LABEL_SIGMA1);
                double[] sigma2Array = p.getAsArray(LABEL_SIGMA2);

                // fit s1,2 = polynomial(frame)
                DefocusFunction polynomS1;
                DefocusFunction polynomS2;
                try {
                    polynomS1 = polynomialFitter.fitParams(defocusModel, framesArrayOfZ, sigma1Array, polyFitMaxIters);
                    polynomS2 = polynomialFitter.fitParams(defocusModel, framesArrayOfZ, sigma2Array, polyFitMaxIters);
                } catch(TooManyEvaluationsException e) {
                    //discard not converged
                    //IJ.log(e.toString());
                    continue;
                } catch(ArrayIndexOutOfBoundsException ex) {
                    // discard: no detections!
                    continue;
                }

                // defocus out of range?
                if(checkIfDefocusIsInRange && (!isInZRange(polynomS1.getC()) || !isInZRange(polynomS2.getC()))) {
                    continue;
                }
                // find the center point between the minima of the two polynomials and shift the origin
                double intersection = (polynomS1.getC() + polynomS2.getC()) / 2;
                if(!hasEnoughData(framesArrayOfZ, intersection) || (checkIfDefocusIsInRange && !isInZRange(intersection))) {
                    continue;
                }
                allPolynomsS1.add(polynomS1);
                allPolynomsS2.add(polynomS2);
                usedPositions.add(p);

                // save values used for fitting for this molecule
                sigma1Arrays.add(sigma1Array);
                sigma2Arrays.add(sigma2Array);
                framesArrays.add(framesArrayOfZ);
            } catch(TooManyEvaluationsException ex) {
                // discard fits that do not converge
            }
        }
        if(framesArrays.size() < 1) {
            throw new NoMoleculesFittedException("Could not fit a polynomial in any bead position.");
        }
        allFrames = flattenListOfArrays(framesArrays);
        allSigma1s = flattenListOfArrays(sigma1Arrays);
        allSigma2s = flattenListOfArrays(sigma2Arrays);
        polynomS1Final = polynomialFitter.fitParams(defocusModel, allFrames, allSigma1s, finalPolyFitMaxIters);
        polynomS2Final = polynomialFitter.fitParams(defocusModel, allFrames, allSigma2s, finalPolyFitMaxIters);

        IJ.showProgress(1);
    }

    protected static PSFSeparator fitFixedAngle(double angle, ImagePlus imp, final Roi roi, final IFilterUI filter, final IDetectorUI detector, final ICalibrationEstimatorUI estimator, DefocusFunction defocusModel) {
        estimator.setAngle(angle);
        estimator.setDefocusModel(defocusModel);
        estimator.resetThreadLocal(); // angle changed so we need to discard the old threadlocal implementations
        // fit stack again with fixed angle
        final PSFSeparator separator = new PSFSeparator(estimator.getFitradius());
        final ImageStack stack = imp.getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        Loop.withIndex(1, stack.getSize() + 1, new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                //fit elliptic Gaussians
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi.getBounds());
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
                fp.setMask(roi.getMask());
                Thresholder.setCurrentImage(fp);
                List<Molecule> fits = estimator.getThreadLocalImplementation().estimateParameters(fp,
                        Point.applyRoiMask(roi, detector.getThreadLocalImplementation().detectMoleculeCandidates(filter.getThreadLocalImplementation().filterImage(fp))));
                framesProcessed.incrementAndGet();

                for(Molecule fit : fits) {
                    fit.insertParamAt(0, MoleculeDescriptor.LABEL_FRAME, MoleculeDescriptor.Units.UNITLESS, i);
                    separator.add(fit);
                    if (showResultsTable) {
                        IJResultsTable.getResultsTable().addRow(fit);
                    }
                }
                IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
                IJ.showStatus("Fitting " + LABEL_SIGMA1 + " and " + LABEL_SIGMA2 + ": frame " + framesProcessed + " of " + stack.getSize() + "...");
            }
        });
        for (PSFSeparator.Position p : separator.getPositions()) {
            p.sortFitsByFrame();
            p.validate();
        }
        if (showResultsTable) {
            IJResultsTable.getResultsTable().sortTableByFrame();
            IJResultsTable.getResultsTable().deleteColumn(LABEL_Z);     // not applicable here
            IJResultsTable.getResultsTable().deleteColumn(LABEL_Z_REL); // not applicable here
            IJResultsTable.getResultsTable().show();
        }
        return separator;
    }

    private static double bootstrapMeanEstimation(List<Double> values, int resamples, int sampleSize) {
        Random rnd = new Random(System.nanoTime());

        double finalMean = 0;
        for(int i = 0; i < resamples; i++) {
            double intermediateMean = 0;
            for(int j = 0; j < sampleSize; j++) {
                intermediateMean += values.get(rnd.nextInt(values.size()));
            }
            intermediateMean = intermediateMean / sampleSize;
            finalMean += intermediateMean;
        }
        finalMean /= resamples;
        return finalMean;
    }

    /**
     * draws overlay with each detection and also the positions of beads that
     * were used for fitting polynomials
     */
    protected static void drawOverlay(ImagePlus imp, Roi roi, List<Molecule> allFits, List<PSFSeparator.Position> usedPositions) {
        imp.setOverlay(null);
        Rectangle roiBounds = roi.getBounds();

        //allFits
        Map<Integer, List<Molecule>> fitsByFrame = new HashMap<Integer, List<Molecule>>(allFits.size());
        for(Molecule mol : allFits) {
            int frame = (int) mol.getParam(LABEL_FRAME);
            List<Molecule> list;
            if(!fitsByFrame.containsKey(frame)) {
                list = new ArrayList<Molecule>();
                fitsByFrame.put(frame, list);
            } else {
                list = fitsByFrame.get(frame);
            }
            list.add(mol);
        }
        for(Map.Entry<Integer, List<Molecule>> frameFitsEntry : fitsByFrame.entrySet()) {
            int frame = frameFitsEntry.getKey();
            List<Molecule> fits = frameFitsEntry.getValue();
            double[] xAll = new double[fits.size()];
            double[] yAll = new double[fits.size()];
            for(int i = 0; i < fits.size(); i++) {
                Molecule mol = fits.get(i);
                xAll[i] = mol.getX(MoleculeDescriptor.Units.PIXEL) + roiBounds.x;
                yAll[i] = mol.getY(MoleculeDescriptor.Units.PIXEL) + roiBounds.y;
            }
            RenderingOverlay.showPointsInImage(imp, xAll, yAll, frame, Color.BLUE, RenderingOverlay.MARKER_CROSS);
        }

        //centroids of used molecules
        double[] xCentroids = new double[usedPositions.size()];
        double[] yCentroids = new double[usedPositions.size()];
        for(int i = 0; i < xCentroids.length; i++) {
            PSFSeparator.Position p = usedPositions.get(i);
            xCentroids[i] = p.centroidX + roiBounds.x;
            yCentroids[i] = p.centroidY + roiBounds.y;
        }
        RenderingOverlay.showPointsInImage(imp, xCentroids, yCentroids, Color.red, RenderingOverlay.MARKER_CIRCLE);
        //usedFits
        for(PSFSeparator.Position p : usedPositions) {
            double[] frame = p.getAsArray(LABEL_FRAME);
            double[] x = VectorMath.add(p.getAsArray(LABEL_X), roiBounds.x);
            double[] y = VectorMath.add(p.getAsArray(LABEL_Y), roiBounds.y);
            for(int i = 0; i < frame.length; i++) {
                RenderingOverlay.showPointsInImage(imp, new double[]{x[i]}, new double[]{y[i]}, (int) frame[i], Color.RED, RenderingOverlay.MARKER_CROSS);
            }
        }
    }

    /**
     * guess z0 of molecule
     */
    protected double guessZ0(PSFSeparator.Position p) {
        double[] sigma1AsArray = p.getAsArray(LABEL_SIGMA1);
        double[] sigma2AsArray = p.getAsArray(LABEL_SIGMA2);
        double[] intensityAsArray = p.getAsArray(LABEL_INTENSITY);

        double[] ratios = new double[sigma1AsArray.length];
        for(int i = 0; i < intensityAsArray.length; i++) {
            ratios[i] = Math.max(sigma1AsArray[i], sigma2AsArray[i]) / Math.min(sigma1AsArray[i], sigma2AsArray[i]);
            ratios[i] /= intensityAsArray[i];
        }

        ratios = VectorMath.movingAverage(ratios, movingAverageLag);

        int minIdx = 0;
        for(int i = 1; i < ratios.length; i++) {
            if(ratios[i] < ratios[minIdx]) {
                minIdx = i;
            }
        }

        return p.fits.get(minIdx).getParam(LABEL_FRAME);
    }

    private static double[] flattenListOfArrays(List<double[]> list) {
        int allFitsCount = 0;
        for(double[] ds : list) {
            allFitsCount += ds.length;
        }
        double[] retVal = new double[allFitsCount];
        int idx = 0;
        for (double[] aList : list) {
            for (double anAList : aList) {
                retVal[idx++] = anAList;
            }
        }
        return retVal;
    }

    private boolean isInZRange(double z) {
        return z >= -zRange && z <= +zRange;
    }

    private boolean hasEnoughData(double[] framesArray, double intersection) {
        int smallerThanCenter = 0;
        for (double aFramesArray : framesArray) {
            if (aFramesArray < intersection) {
                smallerThanCenter++;
            }
        }
        int greaterThanCenter = framesArray.length - smallerThanCenter;

        return !(smallerThanCenter < minFitsInZRange
                || greaterThanCenter < minFitsInZRange
                || framesArray.length < minimumFitsCount);
    }

    protected static List<PSFSeparator.Position> filterPositions(PSFSeparator fits) {
        List<PSFSeparator.Position> ret = new ArrayList<PSFSeparator.Position>();
        for (PSFSeparator.Position fit : fits.getPositions()) {
            if (fit.getSize() >= minimumFitsCount) {
                ret.add(fit);
            }
        }
        return ret;
    }

    protected static void drawSigmaPlots(Plot plot, double[] xVals, SigmaPlotConfig cfg) {
        // add points
        plot.setColor(cfg.allSigma1sColor);
        plot.addPoints(cfg.allFrames, cfg.allSigma1s, Plot.CROSS);
        plot.setColor(cfg.allSigma2sColor);
        plot.addPoints(cfg.allFrames, cfg.allSigma2s, Plot.CROSS);

        // add polynomials
        for(int i = 0; i < cfg.allPolynomsS1.size(); i++) {
            double[] sigma1Vals = new double[xVals.length];
            double[] sigma2Vals = new double[xVals.length];
            for(int j = 0; j < sigma1Vals.length; j++) {
                sigma1Vals[j] = cfg.allPolynomsS1.get(i).value(xVals[j]);
                sigma2Vals[j] = cfg.allPolynomsS2.get(i).value(xVals[j]);
            }
            plot.setColor(cfg.allPolynomsS1Color);
            plot.addPoints(xVals, sigma1Vals, Plot.LINE);
            plot.setColor(cfg.allPolynomsS2Color);
            plot.addPoints(xVals, sigma2Vals, Plot.LINE);
        }

        // add final fitted curves
        double[] sigma1ValsAll = new double[xVals.length];
        double[] sigma2ValsAll = new double[xVals.length];
        for(int j = 0; j < sigma1ValsAll.length; j++) {
            sigma1ValsAll[j] = cfg.polynomS1Final.value(xVals[j]);
            sigma2ValsAll[j] = cfg.polynomS2Final.value(xVals[j]);
        }
        plot.setColor(cfg.polynomS1FinalColor);
        plot.addPoints(xVals, sigma1ValsAll, Plot.LINE);
        plot.setColor(cfg.polynomS2FinalColor);
        plot.addPoints(xVals, sigma2ValsAll, Plot.LINE);

        //legend
        plot.setColor(cfg.polynomS1FinalColor);
        plot.addLabel(cfg.legend1X, cfg.legend1Y, cfg.legend1Label);
        plot.setColor(cfg.polynomS2FinalColor);
        plot.addLabel(cfg.legend2X, cfg.legend2Y, cfg.legend2Label);
    }

    private void showHistoImages() {
        FloatProcessor a1 = new FloatProcessor(1, allPolynomsS1.size());
        FloatProcessor a2 = new FloatProcessor(1, allPolynomsS1.size());
        FloatProcessor b1 = new FloatProcessor(1, allPolynomsS1.size());
        FloatProcessor b2 = new FloatProcessor(1, allPolynomsS1.size());
        FloatProcessor cdif = new FloatProcessor(1, allPolynomsS1.size());

        for(int i = 0; i < allPolynomsS1.size(); i++) {
            a1.setf(i, (float) allPolynomsS1.get(i).getA());
            b1.setf(i, (float) allPolynomsS1.get(i).getB());
            a2.setf(i, (float) allPolynomsS2.get(i).getA());
            b2.setf(i, (float) allPolynomsS2.get(i).getB());
            cdif.setf(i, (float) (allPolynomsS2.get(i).getC() - allPolynomsS1.get(i).getC()));
        }
        new ImagePlus("a1", a1).show();
        new ImagePlus("a2", a2).show();
        new ImagePlus("b1", b1).show();
        new ImagePlus("b2", b2).show();
        new ImagePlus("cdif", cdif).show();
    }

    private void dumpShiftedPoints() {
        try {
            FileWriter fw = new FileWriter("d:\\dump.txt");
            fw.append("allFrames:\n");
            fw.append(Arrays.toString(allFrames));
            fw.append("\nallSigma1:\n");
            fw.append(Arrays.toString(allSigma1s));
            fw.append("\nallSigma2:\n");
            fw.append(Arrays.toString(allSigma2s));
            fw.close();
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    @Override
    public void drawSigmaPlots() {
        // config
        SigmaPlotConfig cfg = new SigmaPlotConfig();
        cfg.allFrames = allFrames;
        cfg.allSigma1s = allSigma1s;
        cfg.allSigma2s = allSigma2s;
        cfg.allPolynomsS1 = allPolynomsS1;
        cfg.allPolynomsS2 = allPolynomsS2;
        cfg.polynomS1Final = polynomS1Final;
        cfg.polynomS2Final = polynomS2Final;
        cfg.allSigma1sColor = new Color(255, 200, 200);
        cfg.allSigma2sColor = new Color(200, 200, 255);
        cfg.allPolynomsS1Color = new Color(255, 230, 230);
        cfg.allPolynomsS2Color = new Color(230, 230, 255);
        cfg.polynomS1FinalColor = new Color(255, 0, 0);
        cfg.polynomS2FinalColor = new Color(0, 0, 255);
        cfg.legend1X = 0.1;
        cfg.legend1Y = 0.8;
        cfg.legend1Label = "sigma1";
        cfg.legend2X = 0.1;
        cfg.legend2Y = 0.9;
        cfg.legend2Label = "sigma2";

        // create and setup plot
        Plot plot = new Plot("Sigma", "z [nm]", "sigma [px]", null, (float[]) null);
        plot.setSize(1024, 768);
        plot.setLimits(-2*zRange, +2*zRange, 0, stageStep);
        double[] xVals = new double[(int)(2*zRange/stageStep) * 2 + 1];
        for(int val = -2*(int)zRange, i = 0; val <= +2*(int)zRange; val += stageStep, i++) {
            xVals[i] = val;
        }
        plot.draw();

        // plot
        drawSigmaPlots(plot, xVals, cfg);

        // display
        plot.show();
    }

    protected static class SigmaPlotConfig {
        public double[] allFrames;
        public double[] allSigma1s;
        public double[] allSigma2s;
        public List<DefocusFunction> allPolynomsS1;
        public List<DefocusFunction> allPolynomsS2;
        public DefocusFunction polynomS1Final;
        public DefocusFunction polynomS2Final;

        public Color allSigma1sColor;
        public Color allSigma2sColor;
        public Color allPolynomsS1Color;
        public Color allPolynomsS2Color;
        public Color polynomS1FinalColor;
        public Color polynomS2FinalColor;

        public double legend1X;
        public double legend1Y;
        public String legend1Label;
        public double legend2X;
        public double legend2Y;
        public String legend2Label;
    }
}
