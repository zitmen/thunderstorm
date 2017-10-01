package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.*;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.ImagePlus;
import ij.gui.Roi;

public final class CalibrationProcessFactory {

    public static AbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                   DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2, boolean z0InMiddleOfStack) {
        if (calibrationEstimatorUI instanceof BiplaneCalibrationEstimatorUI) {
            return new BiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (BiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2, z0InMiddleOfStack);
        } else if (calibrationEstimatorUI instanceof AstigmaticBiplaneCalibrationEstimatorUI) {
            return new AstigmaticBiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (AstigmaticBiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2, z0InMiddleOfStack);
        } else {
            throw new IllegalArgumentException("Unknown instance of biplane calibration estimator!");
        }
    }

    public static AbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                      DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi, boolean z0InMiddleOfStack) {
        if (calibrationEstimatorUI instanceof AstigmatismCalibrationEstimatorUI) {
            return new AstigmaticCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (AstigmatismCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp, roi, z0InMiddleOfStack);
        } else if (calibrationEstimatorUI instanceof PhasorAstigmatismCalibrationEstimatorUI) {
            return new PhasorAstigmaticCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (PhasorAstigmatismCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp, roi, z0InMiddleOfStack);
        } else {
            throw new IllegalArgumentException("Unknown instance of astigmatic calibration estimator!");
        }
    }
}
