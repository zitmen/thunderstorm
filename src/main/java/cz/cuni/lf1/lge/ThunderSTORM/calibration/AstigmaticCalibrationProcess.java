package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.AstigmatismCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.ImagePlus;
import ij.gui.Roi;

import java.util.Collection;

public class AstigmaticCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp;
    Roi roi;

    // results
    private PSFSeparator beadFits;

    public AstigmaticCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, AstigmatismCalibrationEstimatorUI calibrationEstimatorUI, DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi) {
        super(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp = imp;
        this.roi = roi;
    }

    @Override
    protected Collection<Position> fitPositions(double angle) {
        beadFits = fitFixedAngle(angle, imp, roi, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);
        return beadFits.getPositions();
    }

    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        return defocusModel.getCalibration(angle, polynomS1Final, polynomS2Final);
    }

    public void drawOverlay() {
        drawOverlay(imp, roi, beadFits.getAllFits(), usedPositions);
    }
}
