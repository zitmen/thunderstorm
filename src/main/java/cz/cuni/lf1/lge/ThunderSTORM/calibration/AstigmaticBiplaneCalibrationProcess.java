package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.AstigmaticBiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.IBinaryTransform;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;

public class AstigmaticBiplaneCalibrationProcess extends AbstractCalibrationProcess {

    // constants
    private static final String LABEL_SIGMA3 = "sigma3";
    private static final String LABEL_SIGMA4 = "sigma4";
    private static final String LABEL_INTENSITY2 = "intensity2";

    // processing
    ImagePlus imp1, imp2;
    Roi roi1, roi2;

    // results
    private Homography.TransformationMatrix transformationMatrix;
    private PSFSeparator beadFits1, beadFits2;
    private double angle1, angle2;
    private DefocusFunction polyS11, polyS12, polyS21, polyS22;

    // sub-results used for plots
    private ArrayList<DefocusFunction> allPolyS11, allPolyS12, allPolyS21, allPolyS22;
    private double[] allFrames1, allFrames2;
    private double[] allSigma11s, allSigma12s, allSigma21s, allSigma22s;

    public AstigmaticBiplaneCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, AstigmaticBiplaneCalibrationEstimatorUI calibrationEstimatorUI,
                                               DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2) {
        super(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp1 = imp1;
        this.imp2 = imp2;
        this.roi1 = roi1;
        this.roi2 = roi2;
    }

    @Override
    public void runCalibration() {
        Map<PSFSeparator.Position, PSFSeparator.Position> pos12 = fitPositions();
        IJ.log("angle1 = " + angle1);
        IJ.log("angle2 = " + angle2);
        IJ.log("Homography transformation matrix: " + transformationMatrix.toString());

        fitQuadraticPolynomials(pos12.keySet());
        polyS11 = polynomS1Final;
        polyS12 = polynomS2Final;
        allPolyS11 = allPolynomsS1;
        allPolyS12 = allPolynomsS2;
        allFrames1 = allFrames;
        allSigma11s = allSigma1s;
        allSigma12s = allSigma2s;
        IJ.log("s11 = " + polynomS1Final.toString());
        IJ.log("s12 = " + polynomS2Final.toString());

        fitQuadraticPolynomials(pos12.values());
        polyS21 = polynomS1Final;
        polyS22 = polynomS2Final;
        allPolyS21 = allPolynomsS1;
        allPolyS22 = allPolynomsS2;
        allFrames2 = allFrames;
        allSigma21s = allSigma1s;
        allSigma22s = allSigma2s;
        IJ.log("s21 = " + polynomS1Final.toString());
        IJ.log("s22 = " + polynomS2Final.toString());
    }

    @SuppressWarnings("unchecked")
    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        DefocusCalibration cal1 = defocusModel.getCalibration(angle1, null, polyS11, polyS12);
        DefocusCalibration cal2 = defocusModel.getCalibration(angle2, null, polyS21, polyS22);
        return new DoubleDefocusCalibration(cal1.name, transformationMatrix, cal1, cal2);
    }

    @Override
    public void drawSigmaPlots() {
        // config plane 1
        SigmaPlotConfig cfg1 = new SigmaPlotConfig();
        cfg1.allFrames = allFrames1;
        cfg1.allSigma1s = allSigma11s;
        cfg1.allSigma2s = allSigma12s;
        cfg1.allPolynomsS1 = allPolyS11;
        cfg1.allPolynomsS2 = allPolyS12;
        cfg1.polynomS1Final = polyS11;
        cfg1.polynomS2Final = polyS12;
        cfg1.allSigma1sColor = new Color(255, 200, 200);
        cfg1.allSigma2sColor = new Color(200, 200, 255);
        cfg1.allPolynomsS1Color = new Color(255, 230, 230);
        cfg1.allPolynomsS2Color = new Color(230, 230, 255);
        cfg1.polynomS1FinalColor = new Color(255, 0, 0);
        cfg1.polynomS2FinalColor = new Color(0, 0, 255);
        cfg1.legend1X = 0.1;
        cfg1.legend1Y = 0.8;
        cfg1.legend1Label = "sigma11";
        cfg1.legend2X = 0.1;
        cfg1.legend2Y = 0.9;
        cfg1.legend2Label = "sigma12";

        // config plane 2
        SigmaPlotConfig cfg2 = new SigmaPlotConfig();
        cfg2.allFrames = allFrames2;
        cfg2.allSigma1s = allSigma21s;
        cfg2.allSigma2s = allSigma22s;
        cfg2.allPolynomsS1 = allPolyS21;
        cfg2.allPolynomsS2 = allPolyS22;
        cfg2.polynomS1Final = polyS21;
        cfg2.polynomS2Final = polyS22;
        cfg2.allSigma1sColor = new Color(255, 200, 255);
        cfg2.allSigma2sColor = new Color(200, 255, 255);
        cfg2.allPolynomsS1Color = new Color(255, 230, 255);
        cfg2.allPolynomsS2Color = new Color(230, 255, 255);
        cfg2.polynomS1FinalColor = new Color(255, 0, 255);
        cfg2.polynomS2FinalColor = new Color(0, 255, 255);
        cfg2.legend1X = 0.2;
        cfg2.legend1Y = 0.8;
        cfg2.legend1Label = "sigma21";
        cfg2.legend2X = 0.2;
        cfg2.legend2Y = 0.9;
        cfg2.legend2Label = "sigma22";

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
        drawSigmaPlots(plot, xVals, cfg1);
        drawSigmaPlots(plot, xVals, cfg2);

        // display
        plot.show();
    }

    @Override
    protected double guessZ0(PSFSeparator.Position p) {
        double[] sigma1AsArray = p.getAsArray(LABEL_SIGMA1);
        double[] sigma2AsArray = p.getAsArray(LABEL_SIGMA2);
        double[] sigma3AsArray = p.getAsArray(LABEL_SIGMA3);
        double[] sigma4AsArray = p.getAsArray(LABEL_SIGMA4);
        double[] intensity1AsArray = p.getAsArray(LABEL_INTENSITY);
        double[] intensity2AsArray = p.getAsArray(LABEL_INTENSITY2);

        double[] ratios = new double[sigma1AsArray.length];
        for(int i = 0; i < intensity1AsArray.length; i++) {
            double ratio1 = (Math.max(sigma1AsArray[i], sigma2AsArray[i]) / Math.min(sigma1AsArray[i], sigma2AsArray[i])) / intensity1AsArray[i];
            double ratio2 = (Math.max(sigma3AsArray[i], sigma4AsArray[i]) / Math.min(sigma3AsArray[i], sigma4AsArray[i])) / intensity2AsArray[i];
            ratios[i] = (ratio1 + ratio2) / 2.0;
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

    protected Map<PSFSeparator.Position, PSFSeparator.Position> fitPositions() {
        angle1 = estimateAngle(imp1, roi1);
        angle2 = estimateAngle(imp2, roi2);
        if (Double.isNaN(angle1) || Double.isInfinite(angle1)) angle1 = 0.0;
        if (Double.isNaN(angle2) || Double.isInfinite(angle2)) angle2 = 0.0;
        beadFits1 = fitFixedAngle(angle1, imp1, roi1, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);
        beadFits2 = fitFixedAngle(angle2, imp2, roi2, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);
        List<PSFSeparator.Position> fits1 = filterPositions(beadFits1);
        List<PSFSeparator.Position> fits2 = filterPositions(beadFits2);

        IJ.showStatus("Estimating homography between the planes...");
        transformationMatrix = Homography.estimateTransform(
                (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), fits1,
                (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), fits2);
        if (transformationMatrix == null) {
            throw new TransformEstimationFailedException("Could not estimate a transform between the planes!");
        }
        return Homography.mergePositions(transformationMatrix, new IBinaryTransform<PSFSeparator.Position>() {
                @Override
                public void map(PSFSeparator.Position pos1, PSFSeparator.Position pos2) {
                    // map both ways, because both points will be used for curve fitting, thus z0 estimate must be the same for both of them!
                    pos1.setFromArray(LABEL_SIGMA3, MoleculeDescriptor.Units.PIXEL, pos2.getAsArray(LABEL_SIGMA1, MoleculeDescriptor.Units.PIXEL));
                    pos1.setFromArray(LABEL_SIGMA4, MoleculeDescriptor.Units.PIXEL, pos2.getAsArray(LABEL_SIGMA2, MoleculeDescriptor.Units.PIXEL));
                    pos1.setFromArray(LABEL_INTENSITY2, MoleculeDescriptor.Units.PHOTON, pos2.getAsArray(LABEL_INTENSITY, MoleculeDescriptor.Units.PHOTON));

                    pos2.setFromArray(LABEL_SIGMA3, MoleculeDescriptor.Units.PIXEL, pos1.getAsArray(LABEL_SIGMA1, MoleculeDescriptor.Units.PIXEL));
                    pos2.setFromArray(LABEL_SIGMA4, MoleculeDescriptor.Units.PIXEL, pos1.getAsArray(LABEL_SIGMA2, MoleculeDescriptor.Units.PIXEL));
                    pos2.setFromArray(LABEL_INTENSITY2, MoleculeDescriptor.Units.PHOTON, pos1.getAsArray(LABEL_INTENSITY, MoleculeDescriptor.Units.PHOTON));
                }
            }, (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), fits1, (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), fits2);
    }

    public void drawOverlay() {
        // TODO: this doesn't work
        // NullPointerException at cz.cuni.lf1.lge.ThunderSTORM.calibration.AbstractCalibrationProcess.drawOverlay(AbstractCalibrationProcess.java:321)
        // -- it likely comes from LABEL_FRAME array
        //drawOverlay(imp1, roi1, beadFits1.getAllFits(), usedPositions);
        //drawOverlay(imp2, roi2, beadFits2.getAllFits(), Homography.transformPositions(transformationMatrix,
        //        usedPositions, (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight()));
    }
}
