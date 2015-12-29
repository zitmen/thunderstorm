package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.AstigmatismCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

public class AstigmaticCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp;
    Roi roi;

    // results
    private PSFSeparator beadFits;

    public AstigmaticCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, AstigmatismCalibrationEstimatorUI calibrationEstimatorUI,
                                        DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi) {
        super(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp = imp;
        this.roi = roi;
    }

    @Override
    public void runCalibration() {
        angle = estimateAngle(imp, roi);
        IJ.log("angle = " + angle);

        beadFits = fitFixedAngle(angle, imp, roi, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);
        fitQuadraticPolynomials(beadFits.getPositions());
        IJ.log("s1 = " + polynomS1Final.toString());
        IJ.log("s2 = " + polynomS2Final.toString());
    }

    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        return defocusModel.getCalibration(angle, null, polynomS1Final, polynomS2Final);
    }

    @Override
    public void drawOverlay() {
        drawOverlay(imp, roi, beadFits.getAllFits(), usedPositions);
    }
}
