package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
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

/**
 *
 */
public class CalibrationProcess {

    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    CalibrationEstimatorUI calibrationEstimatorUI;
    double stageStep;
    ImagePlus imp;
    Roi roi;
    //results
    private double angle = Double.NaN;
    private List<Position> beadPositions;
    private List<Position> usedPositions;
    private QuadraticFunction polynomS2Final;
    private QuadraticFunction polynomS1Final;
    private ArrayList<QuadraticFunction> allPolynomsS1;
    private ArrayList<QuadraticFunction> allPolynomsS2;
    private double[] allFrames;
    private double[] allSigma1s;
    private double[] allSigma2s;

    public CalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, CalibrationEstimatorUI calibrationEstimatorUI, double stageStep, ImagePlus imp, Roi roi) {
        this.selectedFilterUI = selectedFilterUI;
        this.selectedDetectorUI = selectedDetectorUI;
        this.calibrationEstimatorUI = calibrationEstimatorUI;
        this.stageStep = stageStep;
        this.imp = imp;
        this.roi = roi;
    }

    /**
     * Estimates the rotation angle of the cylindrical lens. If the lens is
     * aligned with camera or the angle is known, you can use setAngle(double)
     * instead.
     *
     */
    public void estimateAngle() {
        final List<Double> angles = Collections.synchronizedList(new ArrayList());
        final ImageStack stack = IJ.getImage().getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        final IEstimatorUI threadLocalEstimatorUI = calibrationEstimatorUI;
        Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi);
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
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
        List<Double> sins = new ArrayList<Double>(angles);
        List<Double> coss = new ArrayList<Double>(angles);
        for(int i = 0; i < angles.size(); i++) {
            double sin = MathProxy.sin(MathProxy.toRadians(sins.get(i) * 4));
            double cos = MathProxy.cos(MathProxy.toRadians(coss.get(i) * 4));
            sins.set(i, sin);
            coss.set(i, cos);
        }
        double sin = bootstrapMeanEstimation(sins, 100, angles.size());
        double cos = bootstrapMeanEstimation(coss, 100, angles.size());
        angle = MathProxy.toDegrees(MathProxy.atan2(sin, cos)) / 4;
    }

    public void fitQuadraticPolynomials() {
        beadPositions = fitFixedAngle();

        //fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
        IterativeQuadraticFitting quadraticFitter = new IterativeQuadraticFitting();
        allPolynomsS1 = new ArrayList<QuadraticFunction>();
        allPolynomsS2 = new ArrayList<QuadraticFunction>();

        List<double[]> framesArrays = new ArrayList<double[]>();
        List<double[]> sigma1Arrays = new ArrayList<double[]>();
        List<double[]> sigma2Arrays = new ArrayList<double[]>();

        AtomicInteger moleculesProcessed = new AtomicInteger(0);
        usedPositions = new ArrayList<Position>();
        for(Position p : beadPositions) {
            moleculesProcessed.incrementAndGet();
            IJ.showProgress(0.9 + 0.1 * (double) moleculesProcessed.intValue() / (double) beadPositions.size());
            IJ.showStatus("Fitting polynoms: molecule " + moleculesProcessed + " of " + beadPositions.size() + "...");

            double[] framesArray = p.getFramesAsArray();
            try {
                if(framesArray.length < 20) {
                    continue;
                }
                //fit s1,2 = polynomial(frame)
                QuadraticFunction polynomS1 = quadraticFitter.fitParams(framesArray, p.getSigma1AsArray());
                QuadraticFunction polynomS2 = quadraticFitter.fitParams(framesArray, p.getSigma2AsArray());

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
                sigma1Arrays.add(p.getSigma1AsArray());
                sigma2Arrays.add(p.getSigma2AsArray());
                double[] shiftedFrames = framesArray.clone();
                for(int i = 0; i < shiftedFrames.length; i++) {
                    shiftedFrames[i] -= intersection;
                }
                framesArrays.add(shiftedFrames);
            } catch(TooManyEvaluationsException ex) {
                IJ.log(ex.getMessage());
            }
        }

        allFrames = flattenListOfArrays(framesArrays);
        allSigma1s = flattenListOfArrays(sigma1Arrays);
        allSigma2s = flattenListOfArrays(sigma2Arrays);
        polynomS1Final = quadraticFitter.fitParams(allFrames, allSigma1s);
        polynomS2Final = quadraticFitter.fitParams(allFrames, allSigma2s);

        if(allPolynomsS1.size() < 1) {
            throw new RuntimeException("Could not fit a parabola in any location.");
        }
        
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

    protected List<Position> fitFixedAngle() {
        calibrationEstimatorUI.setAngle(angle);
        calibrationEstimatorUI.resetThreadLocal(); //angle changed so we need to discard the old threadlocal implementations
        //fit stack again with fixed angle
        final PSFSeparator separator = new PSFSeparator(calibrationEstimatorUI.getFitradius() / 3);
        final ImageStack stack = imp.getStack();
        final AtomicInteger framesProcessed = new AtomicInteger(0);
        Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                //fit elliptic gaussians
                ImageProcessor ip = stack.getProcessor(i);
                ip.setRoi(roi);
                FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
                Thresholder.setCurrentImage(fp);
                Vector<Molecule> fits = calibrationEstimatorUI.getThreadLocalImplementation().estimateParameters(fp,
                        Point.applyRoiMask(roi, selectedDetectorUI.getThreadLocalImplementation().detectMoleculeCandidates(selectedFilterUI.getThreadLocalImplementation().filterImage(fp))));
                framesProcessed.incrementAndGet();

                for(Molecule fit : fits) {
                    separator.add(fit, i);
                }
                IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
                IJ.showStatus("Fitting " + LABEL_SIGMA1 + " and " + LABEL_SIGMA2 + ": frame " + framesProcessed + " of " + stack.getSize() + "...");
            }
        });
        //group fits from the same bead through z-stack
        return separator.getPositions();
    }

    private boolean isInZRange(double z) {
        return z > 0 && z <= imp.getStackSize();
    }

    private boolean hasEnoughData(double[] framesArray, double intersection) {
        int minPts = (int) MathProxy.max(10, 0.3 * framesArray.length);

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

    /**
     * @return the beadPositions
     */
    public List<Position> getBeadPositions() {
        return beadPositions;
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
    public QuadraticFunction getPolynomS2Final() {
        return polynomS2Final;
    }

    /**
     * @return the polynomS1Final
     */
    public QuadraticFunction getPolynomS1Final() {
        return polynomS1Final;
    }

    /**
     * @return the allPolynomsS1
     */
    public ArrayList<QuadraticFunction> getAllPolynomsS1() {
        return allPolynomsS1;
    }

    /**
     * @return the allPolynomsS2
     */
    public ArrayList<QuadraticFunction> getAllPolynomsS2() {
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
}
