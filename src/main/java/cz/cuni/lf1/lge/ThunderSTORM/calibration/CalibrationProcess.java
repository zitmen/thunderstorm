package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_ANGLE;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.CalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.stat.ranking.NaturalRanking;

/**
 *
 */
public class CalibrationProcess {

    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    CalibrationEstimatorUI calibrationEstimatorUI;
    double stageStep;
    double zRange; //in frames
    ImagePlus imp;
    Roi roi;
    //results
    private double angle = Double.NaN;
    private PSFSeparator beadFits;
    private List<Position> usedPositions;
    private DefocusFunction polynomS2Final;
    private DefocusFunction polynomS1Final;
    private ArrayList<DefocusFunction> allPolynomsS1;
    private ArrayList<DefocusFunction> allPolynomsS2;
    private double[] allFrames;
    private double[] allSigma1s;
    private double[] allSigma2s;

    public CalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, CalibrationEstimatorUI calibrationEstimatorUI, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi) {
        this.selectedFilterUI = selectedFilterUI;
        this.selectedDetectorUI = selectedDetectorUI;
        this.calibrationEstimatorUI = calibrationEstimatorUI;
        this.stageStep = stageStep;
        this.imp = imp;
        this.roi = roi;
        this.zRange = zRangeLimit / stageStep; //convert from nm to frames
    }

    /**
     * Estimates the rotation angle of the cylindrical lens. If the lens is
     * aligned with camera or the angle is known, you can use setAngle(double)
     * instead.
     *
     */
    public void estimateAngle() {
        final List<Double> angles = Collections.synchronizedList(new ArrayList());
        final ImageStack stack = imp.getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        final IEstimatorUI threadLocalEstimatorUI = calibrationEstimatorUI;
        Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi.getBounds());
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
                fp.setMask(roi.getMask());
                Thresholder.setCurrentImage(fp);
                Vector<Molecule> fits = threadLocalEstimatorUI.getThreadLocalImplementation().estimateParameters(fp,
                        Point.applyRoiMask(roi, selectedDetectorUI.getThreadLocalImplementation().detectMoleculeCandidates(selectedFilterUI.getThreadLocalImplementation().filterImage(fp))));
                framesProcessed.incrementAndGet();

                for(Iterator<Molecule> iterator = fits.iterator(); iterator.hasNext();) {
                    Molecule psf = iterator.next();
                    double s1 = psf.getParam(LABEL_SIGMA1);
                    double s2 = psf.getParam(LABEL_SIGMA2);
                    double ratio = s1 / s2;
                    if(ratio > 2 || ratio < 0.5) {
                        continue;
                    }
                    if(ratio < 1.2 && ratio > 0.83) {
                        continue;
                    }
                    angles.add(psf.getParam(LABEL_ANGLE));
                }
                IJ.showProgress(0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
                IJ.showStatus("Determining angle: frame " + framesProcessed + " of " + stack.getSize() + "...");
            }
        });
        //calculation of circular mean
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
        angle = MathProxy.atan2(sin, cos) / 4;
    }

    public void fitQuadraticPolynomials() {
        beadFits = fitFixedAngle();
        List<Position> positions = beadFits.getPositions();

        //fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
        IterativeFitting polynomialFitter = new IterativeFitting();
        allPolynomsS1 = new ArrayList<DefocusFunction>();
        allPolynomsS2 = new ArrayList<DefocusFunction>();

        List<double[]> framesArrays = new ArrayList<double[]>();
        List<double[]> sigma1Arrays = new ArrayList<double[]>();
        List<double[]> sigma2Arrays = new ArrayList<double[]>();

        AtomicInteger moleculesProcessed = new AtomicInteger(0);
        usedPositions = new ArrayList<Position>();
        for(Position p : positions) {
            moleculesProcessed.incrementAndGet();
            IJ.showProgress(0.9 + 0.1 * (double) moleculesProcessed.intValue() / (double) positions.size());
            IJ.showStatus("Fitting polynoms: molecule " + moleculesProcessed + " of " + positions.size() + "...");

            try {
                if(p.getSize() < 20) {
                    continue;
                }
                double z0guess = guessZ0(p);
                p.discardFitsByFrameRange(z0guess - zRange, z0guess + zRange);

                //retrieve values again after filtering out fits not in range
                double[] framesArray = p.getFramesAsArray();
                double[] sigma1AsArray = p.getSigma1AsArray();
                double[] sigma2AsArray = p.getSigma2AsArray();

                //fit s1,2 = polynomial(frame)
                DefocusFunction polynomS1;
                DefocusFunction polynomS2;
                try {
                    polynomS1 = polynomialFitter.fitParams(framesArray, sigma1AsArray, 750);
                    polynomS2 = polynomialFitter.fitParams(framesArray, sigma2AsArray, 750);
                } catch(TooManyEvaluationsException e) {
                    //discard not converged
                    //IJ.log(e.toString());
                    continue;
                } catch(ArrayIndexOutOfBoundsException ex) {
                    // discard: no detections!
                    continue;
                }

                if(!isInZRange(polynomS1.getC()) || !isInZRange(polynomS2.getC())) {//realy bad fit?
                    continue;
                }
                //find the center point between the minima of the two polynomials and shift the origin
                double intersection = (polynomS1.getC() + polynomS2.getC()) / 2;
                polynomS1.shiftInZ(intersection);
                polynomS2.shiftInZ(intersection);
                if(!hasEnoughData(framesArray, intersection) || !isInZRange(intersection)) {
                    continue;
                }
                allPolynomsS1.add(polynomS1);
                allPolynomsS2.add(polynomS2);
                usedPositions.add(p);

                //save values used for fitting for this molecule, subtract moleucle the z-pos so that values from all molecules are aligned
                sigma1Arrays.add(sigma1AsArray);
                sigma2Arrays.add(sigma2AsArray);
                double[] shiftedFrames = framesArray.clone();
                for(int i = 0; i < shiftedFrames.length; i++) {
                    shiftedFrames[i] -= intersection;
                }
                framesArrays.add(shiftedFrames);
            } catch(TooManyEvaluationsException ex) {
                //discard fits that do not converge
            }
        }
        if(framesArrays.size() < 1) {
            throw new NoMoleculesFittedException("Could not fit a polynomial in any bead position.");
        }
        allFrames = flattenListOfArrays(framesArrays);
        allSigma1s = flattenListOfArrays(sigma1Arrays);
        allSigma2s = flattenListOfArrays(sigma2Arrays);
        polynomS1Final = polynomialFitter.fitParams(allFrames, allSigma1s, 2000);
        polynomS2Final = polynomialFitter.fitParams(allFrames, allSigma2s, 2000);

        polynomS1Final = polynomS1Final.convertToNm(stageStep);
        polynomS2Final = polynomS2Final.convertToNm(stageStep);

        IJ.showProgress(1);
    }

    private double[] flattenListOfArrays(List<double[]> list) {
        int allFitsCount = 0;
        for(double[] ds : list) {
            allFitsCount += ds.length;
        }
        double[] retVal = new double[allFitsCount];
        int idx = 0;
        for(int i = 0; i < list.size(); i++) {
            for(int j = 0; j < list.get(i).length; j++) {
                retVal[idx++] = list.get(i)[j];
            }
        }
        return retVal;
    }

    protected PSFSeparator fitFixedAngle() {
        calibrationEstimatorUI.setAngle(angle);
        calibrationEstimatorUI.resetThreadLocal(); //angle changed so we need to discard the old threadlocal implementations
        //fit stack again with fixed angle
        final PSFSeparator separator = new PSFSeparator(calibrationEstimatorUI.getFitradius());
        final ImageStack stack = imp.getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                //fit elliptic gaussians
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi.getBounds());
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
                fp.setMask(roi.getMask());
                Thresholder.setCurrentImage(fp);
                Vector<Molecule> fits = calibrationEstimatorUI.getThreadLocalImplementation().estimateParameters(fp,
                        Point.applyRoiMask(roi, selectedDetectorUI.getThreadLocalImplementation().detectMoleculeCandidates(selectedFilterUI.getThreadLocalImplementation().filterImage(fp))));
                framesProcessed.incrementAndGet();

                for(Molecule fit : fits) {
                    fit.insertParamAt(0, MoleculeDescriptor.LABEL_FRAME, MoleculeDescriptor.Units.UNITLESS, i);
                    separator.add(fit);
                }
                IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
                IJ.showStatus("Fitting " + LABEL_SIGMA1 + " and " + LABEL_SIGMA2 + ": frame " + framesProcessed + " of " + stack.getSize() + "...");
            }
        });
        //group fits from the same bead through z-stack
        return separator;
    }

    private boolean isInZRange(double z) {
        return z > 0 && z <= imp.getStackSize();
    }

    private boolean hasEnoughData(double[] framesArray, double intersection) {
        int minPts = (int) MathProxy.max(10, 0.2 * framesArray.length);

        int smallerThanCenterSigma1 = 0;
        int smallerThanCenterSigma2 = 0;
        for(int i = 0; i < framesArray.length; i++) {
            if(framesArray[i] < intersection) {
                smallerThanCenterSigma1++;
            }
            if(framesArray[i] < intersection) {
                smallerThanCenterSigma2++;
            }
        }
        int greaterThanCenterSigma1 = framesArray.length - smallerThanCenterSigma1;
        int greaterThanCenterSigma2 = framesArray.length - smallerThanCenterSigma2;

        if(smallerThanCenterSigma1 < minPts || greaterThanCenterSigma1 < minPts || smallerThanCenterSigma2 < minPts || greaterThanCenterSigma2 < minPts) {
            return false;
        }
        return true;
    }

    private double bootstrapMeanEstimation(List<Double> values, int resamples, int sampleSize) {
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

    public PolynomialCalibration getCalibration() {
        return new PolynomialCalibration(angle, polynomS1Final, polynomS2Final);
    }

    /**
     * Returns the rotation angle of the cylindrical lens. estimateAngle() or
     * setAngle() must be called before. The angle is in radians and is in range
     * [0,PI/2].
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Sets the rotation angle of the cylindrical lens. The angle is in radians
     * and is in range [0,PI/2].
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    public List<Molecule> getAllFits() {
        return beadFits.getAllFits();
    }

    /**
     * @return the usedPositions
     */
    public List<Position> getUsedPositions() {
        return usedPositions;
    }

    /**
     * @return the polynomS2Final
     */
    public DefocusFunction getPolynomS2Final() {
        return polynomS2Final;
    }

    /**
     * @return the polynomS1Final
     */
    public DefocusFunction getPolynomS1Final() {
        return polynomS1Final;
    }

    /**
     * @return the allPolynomsS1
     */
    public ArrayList<DefocusFunction> getAllPolynomsS1() {
        return allPolynomsS1;
    }

    /**
     * @return the allPolynomsS2
     */
    public ArrayList<DefocusFunction> getAllPolynomsS2() {
        return allPolynomsS2;
    }

    /**
     * @return the allFrames
     */
    public double[] getAllFrames() {
        return allFrames;
    }

    /**
     * @return the allSigma1s
     */
    public double[] getAllSigma1s() {
        return allSigma1s;
    }

    /**
     * @return the allSigma2s
     */
    public double[] getAllSigma2s() {
        return allSigma2s;
    }

    /**
     * guess z0 of molecule
     */
    private double guessZ0(Position p) {
        double[] sigma1AsArray = p.getSigma1AsArray();
        double[] sigma2AsArray = p.getSigma2AsArray();
        double[] framesArray = p.getFramesAsArray();
        double[] intensityAsArray = p.getIntensityAsArray();

        NaturalRanking ranker = new NaturalRanking();
        double[] ratiosAsArray = new double[sigma1AsArray.length];
        for(int i = 0; i < framesArray.length; i++) {
            double ratio = Math.max(sigma1AsArray[i], sigma2AsArray[i]) / Math.min(sigma1AsArray[i], sigma2AsArray[i]);
            ratiosAsArray[i] = ratio;
        }

        double[] ratiosRanks = ranker.rank(ratiosAsArray);
        double[] sigma1Ranks = ranker.rank(sigma1AsArray);
        double[] intensityRanks = ranker.rank(intensityAsArray);

        double minVal = ratiosRanks[0] * sigma1Ranks[0] / intensityRanks[0];
        int minIdx = 0;
        for(int i = 0; i < ratiosRanks.length; i++) {
            double val = ratiosRanks[i] * sigma1Ranks[i] / intensityRanks[i];
            if(val < minVal) {
                minVal = val;
                minIdx = i;
            }
        }

        return framesArray[minIdx];
    }
}
