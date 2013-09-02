package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_ANGLE;
import cz.cuni.lf1.lge.ThunderSTORM.UI.CalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.IterativeQuadraticFitting;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PolynomialCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.CalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.Math;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
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
import javax.swing.UIManager;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.yaml.snakeyaml.Yaml;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import java.awt.Rectangle;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CylindricalLensCalibrationPlugin implements PlugIn {

  double[] avgSigma1Polynom;
  double[] avgSigma2Polynom;
  double angle;
  IFilterUI selectedFilterUI;
  IDetectorUI selectedDetectorUI;
  CalibrationEstimatorUI calibrationEstimatorUI;
  String savePath;
  double stageStep;
  ImagePlus imp;
  Roi roi;

  @Override
  public void run(String arg) {
    GUI.setLookAndFeel();
    //
    imp = IJ.getImage();
    if (imp == null) {
      IJ.error("No image open.");
      return;
    }
    if (imp.getImageStackSize() < 2) {
      IJ.error("Requires a stack.");
      return;
    }
    try {
      //load modules
      calibrationEstimatorUI = new CalibrationEstimatorUI();
      List<IFilterUI> filters = ModuleLoader.getUIModules(IFilterUI.class);
      List<IDetectorUI> detectors = ModuleLoader.getUIModules(IDetectorUI.class);
      List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
      Thresholder.loadFilters(filters);

      // get user options
      if (MacroParser.isRanFromMacro()) {
        //parse macro parameters
        MacroParser parser = new MacroParser(filters, estimators, detectors, null);
        selectedFilterUI = parser.getFilterUI();
        selectedDetectorUI = parser.getDetectorUI();
        parser.getEstimatorUI();
        savePath = Macro.getValue(Macro.getOptions(), "saveto", null);
        stageStep = Double.parseDouble(Macro.getValue(Macro.getOptions(), "stageStep", "10"));
      } else {
        //show dialog
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
          IJ.handleException(e);
        }
        CalibrationDialog dialog;
        dialog = new CalibrationDialog(filters, detectors, estimators);
        dialog.setVisible(true);
        if (dialog.waitForResult() != JOptionPane.OK_OPTION) {
          return;
        }
        selectedFilterUI = dialog.getActiveFilterUI();
        selectedDetectorUI = dialog.getActiveDetectorUI();
        savePath = dialog.getSavePath();
        stageStep = dialog.getStageStep();

        //if recording window is open, record parameters
        if (Recorder.record) {
          MacroParser.recordFilterUI(selectedFilterUI);
          MacroParser.recordDetectorUI(selectedDetectorUI);
          MacroParser.recordEstimatorUI(calibrationEstimatorUI);
          Recorder.recordOption("saveto", savePath.replace("\\", "\\\\"));
          Recorder.recordOption("stageStep", stageStep + "");
        }
      }

      roi = imp.getRoi() != null ? imp.getRoi() : new Roi(0, 0, imp.getWidth(), imp.getHeight());

      estimateAngle();
      IJ.log("angle = " + angle);
      fitQuadraticPolynomial();
      saveToFile(savePath);

    } catch (IOException ex) {
      IJ.error("Could not write calibration file: " + ex.getMessage());
    } catch (Exception ex) {
      IJ.handleException(ex);
    }
  }

  private void estimateAngle() {
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

        for (Iterator<Molecule> iterator = fits.iterator(); iterator.hasNext();) {
          Molecule psf = iterator.next();
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
    List<Double> sins = new ArrayList<Double>(angles);
    List<Double> coss = new ArrayList<Double>(angles);
    for (int i = 0; i < angles.size(); i++) {
      double sin = Math.sin(Math.toRadians(sins.get(i) * 4));
      double cos = Math.cos(Math.toRadians(coss.get(i)) * 4);
      sins.set(i, sin);
      coss.set(i, cos);
    }
    double sin = bootstrapMeanEstimation(sins, 100, angles.size());
    double cos = bootstrapMeanEstimation(coss, 100, angles.size());
    angle = Math.toDegrees(Math.atan2(sin, cos))/4;
  }

  private void fitQuadraticPolynomial() {
    calibrationEstimatorUI.setAngle(angle);
    calibrationEstimatorUI.resetThreadLocal();
    final IEstimatorUI threadLocalEstimatorUI = calibrationEstimatorUI; //create new ThreadLocal wrapper because the underlying estimator was changed
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
        Vector<Molecule> fits = threadLocalEstimatorUI.getThreadLocalImplementation().estimateParameters(fp,
                Point.applyRoiMask(roi, selectedDetectorUI.getThreadLocalImplementation().detectMoleculeCandidates(selectedFilterUI.getThreadLocalImplementation().filterImage(fp))));
        framesProcessed.incrementAndGet();

        for (Molecule fit : fits) {
          separator.add(fit, i);
        }
        IJ.showProgress(0.45 + 0.45 * (double) framesProcessed.intValue() / (double) stack.getSize());
        IJ.showStatus("Fitting " + LABEL_SIGMA1 + " and " + LABEL_SIGMA2 + ": frame " + framesProcessed + " of " + stack.getSize() + "...");
      }
    });
    //group fits from the same bead through z-stack
    List<Position> beadPositions = separator.getPositions();

    //fit a quadratic polynomial to sigma1 = f(zpos) and sigma1 = f(zpos) for each bead
    IterativeQuadraticFitting quadraticFitter = new IterativeQuadraticFitting();
    List<double[]> sigma1Quadratics = new ArrayList<double[]>();
    List<double[]> sigma2Quadratics = new ArrayList<double[]>();
//    StringBuilder sb = new StringBuilder();
    //Locale.setDefault(Locale.ENGLISH);
    AtomicInteger moleculesProcessed = new AtomicInteger(0);
    List<Position> usedPositions = new ArrayList<Position>();
    for (Position p : beadPositions) {
      moleculesProcessed.incrementAndGet();
      IJ.showProgress(0.9 + 0.1 * (double) moleculesProcessed.intValue() / (double) beadPositions.size());
      IJ.showStatus("Fitting polynoms: molecule " + moleculesProcessed + " of " + beadPositions.size() + "...");

      double[] framesArray = p.getFramesAsArray();
      try {
        if (framesArray.length < 20) {
          continue;
        }
        double[] sigma1ParamArray = quadraticFitter.fitParams(framesArray, p.getSigma1AsArray());
        double[] sigma2ParamArray = quadraticFitter.fitParams(framesArray, p.getSigma2AsArray());

        if (!isInZRange(sigma1ParamArray[0]) || !isInZRange(sigma2ParamArray[0])) {
          continue;
        }
        //find the intersection of the two quadratic polynomials and shift the origin to the intersection
        double intersection = IterativeQuadraticFitting.shiftToOrigin(sigma1ParamArray, sigma2ParamArray);
        if (!hasEnoughData(framesArray, intersection) || !isInZRange(intersection)) {
          continue;
        }
        sigma1Quadratics.add(sigma1ParamArray);
        sigma2Quadratics.add(sigma2ParamArray);
        usedPositions.add(p);

        //          sb.append(String.format("fits(%d).z = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(framesArray)));
        //          sb.append(String.format("fits(%d).s1 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigma1AsArray())));
        //          sb.append(String.format("fits(%d).s2 = %s;\n", moleculesProcessed.intValue() + 1, Arrays.toString(p.getSigma2AsArray())));
        //          sb.append(String.format("fits(%d).a1 = %f;\n", moleculesProcessed.intValue() + 1, sigma1ParamArray[1]));
        //          sb.append(String.format("fits(%d).a2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[1]));
        //          sb.append(String.format("fits(%d).b1 = %f;\n", moleculesProcessed.intValue() + 1, sigma1ParamArray[2]));
        //          sb.append(String.format("fits(%d).b2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[2]));
        //          sb.append(String.format("fits(%d).c1 = %f;\n", moleculesProcessed.intValue() + 1, sigma1ParamArray[0]));
        //          sb.append(String.format("fits(%d).c2 = %f;\n", moleculesProcessed.intValue() + 1, sigma2ParamArray[0]));
        //          sb.append(String.format("fits(%d).intersection = %f;\n", moleculesProcessed.intValue() + 1, intersection));

        //showXYplot(add(framesArray, -intersection), add(p.getXAsArray(), -p.getFitsByFrame().get((int) intersection).getX()), add(p.getYAsArray(), -p.getFitsByFrame().get((int) intersection).getY()));

      } catch (TooManyEvaluationsException ex) {
        IJ.log(ex.getMessage());
      }
    }
    drawOverlay(beadPositions, usedPositions);
//    for (int i = 0; i < sigma2Quadratics.size(); i++) {
//      IJ.log(Arrays.toString(sigma1Quadratics.get(i)) + " ; " + Arrays.toString(sigma2Quadratics.get(i)));
//    }

    if (sigma1Quadratics.size() < 1) {
      throw new RuntimeException("Could not fit a parabola in any location.");
    }

    
    drawSigmaPlots(sigma1Quadratics, sigma2Quadratics);
    //average the parameters of the fitted polynomials for each bead
    avgSigma1Polynom = bootstrapMeanEstimationArray(sigma1Quadratics, 100, sigma1Quadratics.size());
    avgSigma2Polynom = bootstrapMeanEstimationArray(sigma2Quadratics, 100, sigma2Quadratics.size());
    convertToNm();
//    sb.append(String.format("a1 = %f;\n", avgSigma1Polynom[1]));
//    sb.append(String.format("a2 = %f;\n", avgSigma2Polynom[1]));
//    sb.append(String.format("b1 = %f;\n", avgSigma1Polynom[2]));
//    sb.append(String.format("b2 = %f;\n", avgSigma2Polynom[2]));
//    sb.append(String.format("c1 = %f;\n", avgSigma1Polynom[0]));
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
    IJ.log(String.format(Locale.ENGLISH, "s1 =  %f*(z%+f)^2 %+f", avgSigma1Polynom[1], -avgSigma1Polynom[0], avgSigma1Polynom[2]));
    IJ.log(String.format(Locale.ENGLISH, "s2 =  %f*(z%+f)^2 %+f", avgSigma2Polynom[1], -avgSigma2Polynom[0], avgSigma2Polynom[2]));
  }

  private boolean isInZRange(double z) {
    return z > 0 && z <= imp.getStackSize();
  }

  private boolean hasEnoughData(double[] framesArray, double intersection) {
    int minPts = (int) Math.max(10, 0.3 * framesArray.length);

    int smallerThanCenterSigma1 = 0;
    int smallerThanCenterSigma2 = 0;
    for (int i = 0; i < framesArray.length; i++) {
      if (framesArray[i] < intersection) {
        smallerThanCenterSigma1++;
      }
      if (framesArray[i] < intersection) {
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
    PolynomialCalibration calibration = new PolynomialCalibration(angle, avgSigma1Polynom, avgSigma2Polynom);
    yaml.dump(calibration, new FileWriter(path));
    IJ.showStatus("Calibration file saved to " + path);
  }

  private void showXYplot(double[] framesArray, double[] x, double[] y) {
    Plot plot = new Plot("X", "frame", "offset");
    plot.setLimits(-50, 50, -2, 2);
    plot.setColor(Color.blue);
    plot.addPoints(framesArray, x, Plot.CROSS);
    plot.addLabel(0.05, 0.8, "x drift");
    plot.draw();
    plot.setColor(Color.red);
    plot.addPoints(framesArray, y, Plot.CROSS);
    plot.addLabel(0.05, 0.9, "y drift");
    plot.show();
  }

  private void drawSigmaPlots(List<double[]> sigma1Quadratics, List<double[]> sigma2Quadratics) {
    int range = imp.getStackSize() / 2;
    Plot plot = new Plot("Sigma", "z[slices]", "sigma");
    plot.setLimits(-range, +range, 0, 10);
    double[] xVals = new double[range * 2 + 1];
    for (int val = -range, i = 0; val <= range; val++, i++) {
      xVals[i] = val;
    }
    plot.draw();
    for (int i = 0; i < sigma1Quadratics.size(); i++) {
      double[] sigma1Vals = new double[xVals.length];
      double[] sigma2Vals = new double[xVals.length];
      double[] params = sigma1Quadratics.get(i);
      double[] params2 = sigma2Quadratics.get(i);
      for (int j = 0; j < sigma1Vals.length; j++) {
        sigma1Vals[j] = Math.sqr(xVals[j] - params[0]) * params[1] + params[2];
        sigma2Vals[j] = Math.sqr(xVals[j] - params2[0]) * params2[1] + params2[2];
      }
      plot.setColor(Color.red);
      plot.addPoints(xVals, sigma1Vals, Plot.LINE);
      plot.setColor(Color.BLUE);
      plot.addPoints(xVals, sigma2Vals, Plot.LINE);
    }

    plot.setColor(Color.red);
    plot.addLabel(0.1, 0.8, "sigma1");
    plot.setColor(Color.blue);
    plot.addLabel(0.1, 0.9, "sigma2");
    plot.show();
  }

  private void drawOverlay(List<Position> allPositions, List<Position> usedPositions) {
    imp.setOverlay(null);
    Rectangle roiBounds = roi.getBounds();
    double[] xCentroids = new double[usedPositions.size()];
    double[] yCentroids = new double[usedPositions.size()];
    for (int i = 0; i < xCentroids.length; i++) {
      Position p = usedPositions.get(i);
      xCentroids[i] = p.centroidX + roiBounds.x;
      yCentroids[i] = p.centroidY + roiBounds.y;
    }
    RenderingOverlay.showPointsInImage(imp, xCentroids, yCentroids, Color.red, RenderingOverlay.MARKER_CIRCLE);
    for (Position p : allPositions) {
      double[] frame = p.getFramesAsArray();
      double[] x = Math.add(p.getXAsArray(), roiBounds.x);
      double[] y = Math.add(p.getYAsArray(), roiBounds.y);
      for (int i = 0; i < frame.length; i++) {
        RenderingOverlay.showPointsInImageSlice(imp, new double[]{x[i]}, new double[]{y[i]}, (int) frame[i], Color.BLUE, RenderingOverlay.MARKER_CROSS);
      }
    }

  }

    private void convertToNm() {
        avgSigma1Polynom[0] *=stageStep;
        avgSigma1Polynom[1] /= stageStep*stageStep;
        
        avgSigma2Polynom[0] *=stageStep;
        avgSigma2Polynom[1] /=stageStep*stageStep;
    }
}
