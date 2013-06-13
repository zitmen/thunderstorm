package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.CalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.IterativeQuadraticFitting;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PolynomialCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.CalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
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
  IFilterUI selectedFilterUI;
  IDetectorUI selectedDetectorUI;
  CalibrationEstimatorUI calibrationEstimatorUI;
  String savePath;
  ImagePlus imp;

  @Override
  public void run(String arg) {
    imp = IJ.getImage();
    if (imp == null) {
      IJ.error("No image open.");
      return;
    }
    if (imp.getImageStackSize() < 2) {
      IJ.error("Requires a stack.");
      return;
    }
    //load modules
    calibrationEstimatorUI = new CalibrationEstimatorUI();
    List<IFilterUI> filters = ThreadLocalWrapper.wrapFilters(ModuleLoader.getUIModules(IFilterUI.class));
    List<IDetectorUI> detectors = ThreadLocalWrapper.wrapDetectors(ModuleLoader.getUIModules(IDetectorUI.class));
    List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
    Thresholder.loadFilters(filters);

    if (MacroParser.isRanFromMacro()) {
      MacroParser parser = new MacroParser(filters, estimators, detectors, null);
      selectedFilterUI = parser.getFilterUI();
      selectedDetectorUI = parser.getDetectorUI();
      parser.getEstimatorUI();
      savePath = Macro.getValue(Macro.getOptions(), "saveto", null);
    } else {
      //show dialog
      CalibrationDialog dialog;
      dialog = new CalibrationDialog(filters, detectors, estimators);
      dialog.setVisible(true);
      if (dialog.waitForResult() != JOptionPane.OK_OPTION) {
        return;
      }
      selectedFilterUI = dialog.getActiveFilterUI();
      selectedDetectorUI = dialog.getActiveDetectorUI();
      savePath = dialog.getSavePath();
      if (Recorder.record) {
        MacroParser.recordFilterUI(selectedFilterUI);
        MacroParser.recordDetectorUI(selectedDetectorUI);
        MacroParser.recordEstimatorUI(calibrationEstimatorUI);
        Recorder.recordOption("saveto", savePath.replace("\\", "\\\\"));
      }
    }
    try {
      estimateAngle();
      IJ.log("angle = " + angle);
      fitQuadraticPolynomial();
      saveToFile(savePath);
    } catch (IOException ex) {
      IJ.log("Could not write calibration file: " + ex.getMessage());
    } catch (Exception ex) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      IJ.log(sw.toString());
    }
  }

  private void estimateAngle() {
    final List<Double> angles = Collections.synchronizedList(new ArrayList());
    final ImageStack stack = IJ.getImage().getStack();
    final AtomicInteger framesProcessed = new AtomicInteger(0);
    final IEstimatorUI threadLocalEstimatorUI = ThreadLocalWrapper.wrap(calibrationEstimatorUI);
    Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
      @Override
      public void run(int i) {
        Vector<PSFInstance> fits = threadLocalEstimatorUI.getImplementation().estimateParameters((FloatProcessor) stack.getProcessor(i).convertToFloat(),
                selectedDetectorUI.getImplementation().detectMoleculeCandidates(
                selectedFilterUI.getImplementation().filterImage((FloatProcessor) stack.getProcessor(i).convertToFloat())));
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
    calibrationEstimatorUI.setAngle(angle);
    final IEstimatorUI threadLocalEstimatorUI = ThreadLocalWrapper.wrap(calibrationEstimatorUI); //create new ThreadLocal wrapper because the underlying estimator was changed
    //fit stack again with fixed angle
    final PSFSeparator separator = new PSFSeparator(calibrationEstimatorUI.getFitradius() / 2);
    final ImageStack stack = imp.getStack();
    final AtomicInteger framesProcessed = new AtomicInteger(0);

    Loop.withIndex(1, stack.getSize(), new Loop.BodyWithIndex() {
      @Override
      public void run(int i) {
        //fit elliptic gaussians
        Vector<PSFInstance> fits = threadLocalEstimatorUI.getImplementation().estimateParameters((FloatProcessor) stack.getProcessor(i).convertToFloat(),
                selectedDetectorUI.getImplementation().detectMoleculeCandidates(
                selectedFilterUI.getImplementation().filterImage((FloatProcessor) stack.getProcessor(i).convertToFloat())));
        framesProcessed.incrementAndGet();

        for (PSFInstance fit : fits) {
          separator.add(fit, i);
        }
        IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
        IJ.showStatus("Fitting " + PSFInstance.SIGMA + " and " + PSFInstance.SIGMA2 + ": frame " + framesProcessed + " of " + stack.getSize() + "...");
      }
    });
    //group fits from the same bead through z-stack
    List<Position> beadPositions = separator.getPositions();

    //fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
    IterativeQuadraticFitting quadraticFitter = new IterativeQuadraticFitting();
    List<double[]> sigmaQuadratics = new ArrayList<double[]>();
    List<double[]> sigma2Quadratics = new ArrayList<double[]>();
//    StringBuilder sb = new StringBuilder();
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

//          sb.append(String.format("fits(%d).z = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(framesArray)));
//          sb.append(String.format("fits(%d).s1 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigmaAsArray())));
//          sb.append(String.format("fits(%d).s2 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigma2AsArray())));
//          sb.append(String.format("fits(%d).a1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[1]));
//          sb.append(String.format("fits(%d).a2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[1]));
//          sb.append(String.format("fits(%d).b1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[2]));
//          sb.append(String.format("fits(%d).b2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[2]));
//          sb.append(String.format("fits(%d).c1 = %f;\n", moleculesProcessed.intValue() + 1, sigmaParamArray[0]));
//          sb.append(String.format("fits(%d).c2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[0]));
//          sb.append(String.format("fits(%d).intersection = %f;\n", moleculesProcessed.intValue() + 1, intersection));
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

    if (sigmaQuadratics.size() < 1) {
      throw new RuntimeException("Could not fit a parabola in any location.");
    }
    //average the parameters of the fitted polynomials for each bead
    avgSigmaPolynom = bootstrapMeanEstimationArray(sigmaQuadratics, 100, sigmaQuadratics.size());
    avgSigma2Polynom = bootstrapMeanEstimationArray(sigma2Quadratics, 100, sigma2Quadratics.size());

//    sb.append(String.format("a1 = %f;\n", avgSigmaPolynom[1]));
//    sb.append(String.format("a2 = %f;\n", avgSigma2Polynom[1]));
//    sb.append(String.format("b1 = %f;\n", avgSigmaPolynom[2]));
//    sb.append(String.format("b2 = %f;\n", avgSigma2Polynom[2]));
//    sb.append(String.format("c1 = %f;\n", avgSigmaPolynom[0]));
//    sb.append(String.format("c2 = %f;\n", avgSigma2Polynom[0]));
//    sb.append("empty_elems = arrayfun(@(s) all(structfun(@isempty,s)), fits);\n"
//            + "fits(empty_elems) = [];");
//    try {
//      FileWriter fw = new FileWriter("d:\\dump.m");
//      fw.append(sb);
//      fw.close();
//    } catch (IOException ex) {
//    }
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
    IJ.showStatus("Calibration file saved to " + path);
  }
}
