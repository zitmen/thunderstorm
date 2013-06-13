package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.CalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.IterativeQuadraticFitting;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PolynomialCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import ij.IJ;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CylindricalLensCalibrationPlugin implements PlugIn {

  double[] avgSigmaPolynom;
  double[] avgSigma2Polynom;
  double angle;
  CalibrationDialog dialog;

  @Override
  public void run(String arg) {
    dialog = new CalibrationDialog();
    dialog.setVisible(true);
    if (dialog.waitForResult() != JOptionPane.OK_OPTION) {
      return;
    }

    try {
      estimateAngle();
      IJ.log("angle = " + angle);
      fitQuadraticPolynomial();
      saveToFile("d:\\calibration.yaml");
    } catch (IOException ex) {
      IJ.log("Could not write calibration file: " + ex.getMessage());
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      IJ.log(sw.toString());
    }
  }

  private void estimateAngle() {
    final ThreadLocalModule<IFilterUI, IFilter> threadLocalFilter = new ThreadLocalModule<IFilterUI, IFilter>(null) {
      @Override
      public IFilter initialValue() {
        return new CompoundWaveletFilter(true);
      }
    };
    Vector<ThreadLocalModule<IFilterUI, IFilter>> filters = new Vector<ThreadLocalModule<IFilterUI, IFilter>>();
    filters.add(threadLocalFilter);
    Thresholder.loadFilters(filters);
    Thresholder.setActiveFilter(0);
    final ThreadLocalModule<IDetectorUI, IDetector> threadLocalDetector = new ThreadLocalModule<IDetectorUI, IDetector>(null) {
      @Override
      public IDetector initialValue() {
        return new CentroidOfConnectedComponentsDetector(false, "mean(I)*2");
      }
    };
    final ThreadLocalModule<IEstimatorUI, IEstimator> threadLocalEstimator = new ThreadLocalModule<IEstimatorUI, IEstimator>(null) {
      @Override
      public IEstimator initialValue() {
        return new MultipleLocationsImageFitting(30, new LSQFitter(new EllipticGaussianWAnglePSF(3, 60)));
      }
    };

    final List<Double> angles = Collections.synchronizedList(new ArrayList());
    final ImageStack stack = IJ.getImage().getStack();
    final AtomicInteger framesProcessed = new AtomicInteger(0);
    Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
      @Override
      public void run(int i) {
        Vector<PSFInstance> fits = threadLocalEstimator.get().estimateParameters((FloatProcessor) stack.getProcessor(i).convertToFloat(),
                threadLocalDetector.get().detectMoleculeCandidates(
                threadLocalFilter.get().filterImage((FloatProcessor) stack.getProcessor(i).convertToFloat())));
        framesProcessed.incrementAndGet();

        for (Iterator<PSFInstance> iterator = fits.iterator(); iterator.hasNext();) {
          PSFInstance psf = iterator.next();
          double s1 = psf.getParam(PSFInstance.SIGMA);
          double s2 = psf.getParam(PSFInstance.SIGMA2);
          double ratio = s1 / s2;
          if (ratio > 2 || ratio < 0.5) {
            continue;
          }
          if (ratio < 1.2 && ratio > 0.83) {
            continue;
          }
          angles.add(psf.getParam("angle"));
        }
        IJ.showProgress(0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
        IJ.showStatus("Determining angle: frame " + framesProcessed + " of " + stack.getSize() + "...");
      }
    });
    angle = bootstrapMeanEstimation(angles, 100, angles.size());
  }

  private void fitQuadraticPolynomial() {
    final ThreadLocalModule<IFilterUI, IFilter> threadLocalFilter = new ThreadLocalModule<IFilterUI, IFilter>(null) {
      @Override
      public IFilter initialValue() {
        return new CompoundWaveletFilter(true);
      }
    };
    Vector<ThreadLocalModule<IFilterUI, IFilter>> filters = new Vector<ThreadLocalModule<IFilterUI, IFilter>>();
    filters.add(threadLocalFilter);
    Thresholder.loadFilters(filters);
    Thresholder.setActiveFilter(0);
    final ThreadLocalModule<IDetectorUI, IDetector> threadLocalDetector = new ThreadLocalModule<IDetectorUI, IDetector>(null) {
      @Override
      public IDetector initialValue() {
        return new CentroidOfConnectedComponentsDetector(false, "mean(I)*2");
      }
    };
    final ThreadLocalModule<IEstimatorUI, IEstimator> threadLocalEstimator = new ThreadLocalModule<IEstimatorUI, IEstimator>(null) {
      @Override
      public IEstimator initialValue() {
        return new MultipleLocationsImageFitting(30, new LSQFitter(new EllipticGaussianPSF(2, angle)));
      }
    };

    final PSFSeparator separator = new PSFSeparator(15);
    final ImageStack stack = IJ.getImage().getStack();
    final AtomicInteger framesProcessed = new AtomicInteger(0);
    Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
      @Override
      public void run(int i) {
        //fit elliptic gaussians
        Vector<PSFInstance> fits = threadLocalEstimator.get().estimateParameters((FloatProcessor) stack.getProcessor(i).convertToFloat(),
                threadLocalDetector.get().detectMoleculeCandidates(
                threadLocalFilter.get().filterImage((FloatProcessor) stack.getProcessor(i).convertToFloat())));
        framesProcessed.incrementAndGet();

        for (PSFInstance fit : fits) {
          separator.add(fit, i);
        }
        IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
        IJ.showStatus("Localizing molecules: frame " + framesProcessed + " of " + stack.getSize() + "...");
      }
    });
    //group fits from the same bead through z-stack
    List<Position> beadPositions = separator.getPositions();

    //fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
    IterativeQuadraticFitting quadraticFitter = new IterativeQuadraticFitting();
    List<double[]> sigmaQuadratics = new ArrayList<double[]>();
    List<double[]> sigma2Quadratics = new ArrayList<double[]>();
    StringBuilder sb = new StringBuilder();
    Locale.setDefault(Locale.ENGLISH);
    AtomicInteger moleculesProcessed = new AtomicInteger(0);
    for (Position p : beadPositions) {
      double[] framesArray = p.getFramesAsArray();
      try {
        double[] sigmaParamArray = quadraticFitter.fitParams(framesArray, p.getSigmaAsArray());
        double[] sigma2ParamArray = quadraticFitter.fitParams(framesArray, p.getSigma2AsArray());

        if (hasEnoughData(framesArray, sigmaParamArray, sigma2ParamArray)) {
          //find the intersection of the two quadratic polynomials and shift the origin to the intersection
          double intersection = IterativeQuadraticFitting.shiftToOrigin(sigmaParamArray, sigma2ParamArray);
          sigmaQuadratics.add(sigmaParamArray);
          sigma2Quadratics.add(sigma2ParamArray);

          sb.append(String.format("fits(%d).z = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(framesArray)));
          sb.append(String.format("fits(%d).s1 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigmaAsArray())));
          sb.append(String.format("fits(%d).s2 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigma2AsArray())));
          sb.append(String.format("fits(%d).a1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[1]));
          sb.append(String.format("fits(%d).a2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[1]));
          sb.append(String.format("fits(%d).b1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[2]));
          sb.append(String.format("fits(%d).b2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[2]));
          sb.append(String.format("fits(%d).c1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[0]));
          sb.append(String.format("fits(%d).c2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[0]));
          sb.append(String.format("fits(%d).intersection = %f;\n", moleculesProcessed.intValue() + 1, intersection));
        }
      } catch (TooManyEvaluationsException ex) {
        IJ.log(ex.getMessage());
      }
      moleculesProcessed.incrementAndGet();
      IJ.showProgress(0.9 + 0.1 * (double) moleculesProcessed.intValue() / (double) beadPositions.size());
      IJ.showStatus("Fitting polynoms: molecule " + moleculesProcessed + " of " + beadPositions.size() + "...");
    }

    for (int i = 0; i < sigma2Quadratics.size(); i++) {
      IJ.log(Arrays.toString(sigmaQuadratics.get(i)) + " ; " + Arrays.toString(sigma2Quadratics.get(i)));
    }
    //average the parameters of the fitted polynomials for each bead
    avgSigmaPolynom = bootstrapMeanEstimationArray(sigmaQuadratics, 100, sigmaQuadratics.size());
    avgSigma2Polynom = bootstrapMeanEstimationArray(sigma2Quadratics, 100, sigma2Quadratics.size());

    sb.append(String.format("a1 = %f;\n", avgSigmaPolynom[1]));
    sb.append(String.format("a2 = %f;\n", avgSigma2Polynom[1]));
    sb.append(String.format("b1 = %f;\n", avgSigmaPolynom[2]));
    sb.append(String.format("b2 = %f;\n", avgSigma2Polynom[2]));
    sb.append(String.format("c1 = %f;\n", avgSigmaPolynom[0]));
    sb.append(String.format("c2 = %f;\n", avgSigma2Polynom[0]));
    sb.append("empty_elems = arrayfun(@(s) all(structfun(@isempty,s)), fits);\n"
            + "fits(empty_elems) = [];");
    try {
      FileWriter fw = new FileWriter("d:\\dump.m");
      fw.append(sb);
      fw.close();
    } catch (IOException ex) {
    }
    IJ.showProgress(1);
    IJ.log("s1: " + Arrays.toString(avgSigmaPolynom));
    IJ.log("s2: " + Arrays.toString(avgSigma2Polynom));
  }

  private boolean hasEnoughData(double[] framesArray, double[] sigmaParamArray, double[] sigma2ParamArray) {
    int minPts = (int) Math.max(20, 0.3 * framesArray.length);

    double sigma1QuadraticCenter = sigmaParamArray[0];
    double sigma2QuadraticCenter = sigma2ParamArray[0];

    int smallerThanCenterSigma1 = 0;
    int smallerThanCenterSigma2 = 0;
    for (int i = 0; i < framesArray.length; i++) {
      if (framesArray[i] < sigma1QuadraticCenter) {
        smallerThanCenterSigma1++;
      }
      if (framesArray[i] < sigma2QuadraticCenter) {
        smallerThanCenterSigma2++;
      }
    }
    int greaterThanCenterSigma1 = framesArray.length - smallerThanCenterSigma1;
    int greaterThanCenterSigma2 = framesArray.length - smallerThanCenterSigma2;

    if (smallerThanCenterSigma1 < minPts || greaterThanCenterSigma1 < minPts || smallerThanCenterSigma2 < minPts || greaterThanCenterSigma2 < minPts) {
      return false;
    }
    return true;
  }

  private double bootstrapMeanEstimation(List<Double> values, int resamples, int sampleSize) {
    Random rnd = new Random(System.nanoTime());

    double finalMean = 0;
    for (int i = 0; i < resamples; i++) {
      double intermediateMean = 0;
      for (int j = 0; j < sampleSize; j++) {
        intermediateMean += values.get(rnd.nextInt(values.size()));
      }
      intermediateMean = intermediateMean / sampleSize;
      finalMean += intermediateMean;
    }
    finalMean /= resamples;
    return finalMean;
  }

  private double[] bootstrapMeanEstimationArray(List<double[]> values, int resamples, int sampleSize) {
    Random rnd = new Random(System.nanoTime());

    double[] finalMeans = new double[values.get(0).length];
    for (int i = 0; i < resamples; i++) {
      double[] intermediateMeans = new double[values.get(0).length];
      for (int j = 0; j < sampleSize; j++) {
        for (int k = 0; k < intermediateMeans.length; k++) {
          intermediateMeans[k] += values.get(rnd.nextInt(values.size()))[k];
        }
      }
      for (int k = 0; k < intermediateMeans.length; k++) {
        intermediateMeans[k] = intermediateMeans[k] / sampleSize;
        finalMeans[k] += intermediateMeans[k];
      }
    }
    for (int i = 0; i < finalMeans.length; i++) {
      finalMeans[i] /= resamples;
    }
    return finalMeans;
  }

  private void saveToFile(String path) throws IOException {
    Yaml yaml = new Yaml();
    PolynomialCalibration calibration = new PolynomialCalibration(angle, avgSigmaPolynom, avgSigma2Polynom);
    yaml.dump(calibration, new FileWriter(path));
  }
}
