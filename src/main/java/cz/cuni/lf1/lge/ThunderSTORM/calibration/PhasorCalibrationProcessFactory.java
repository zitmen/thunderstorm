package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.AstigmaticBiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.PhasorAstigmatismCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.ICalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.ImagePlus;
import ij.gui.Roi;

public final class PhasorCalibrationProcessFactory {

    public static AbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                   DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2, double zzeropos) {
        if (calibrationEstimatorUI instanceof BiplaneCalibrationEstimatorUI) {
            return new BiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (BiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2, zzeropos);
        } else if (calibrationEstimatorUI instanceof AstigmaticBiplaneCalibrationEstimatorUI) {
            return new AstigmaticBiplaneCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (AstigmaticBiplaneCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2,zzeropos);
        } else {
            throw new IllegalArgumentException("Unknown instance of astigmatic calibration estimator!");
        }
    }

    public static PhasorAbstractCalibrationProcess create(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, ICalibrationEstimatorUI calibrationEstimatorUI,
                                                      DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp, Roi roi, double zzeropos) {
        if (calibrationEstimatorUI instanceof PhasorAstigmatismCalibrationEstimatorUI) {
            return new PhasorAstigmaticCalibrationProcess(config, selectedFilterUI, selectedDetectorUI, (PhasorAstigmatismCalibrationEstimatorUI) calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, imp, roi, zzeropos);
        } else {
            throw new IllegalArgumentException("Unknown instance of astigmatic calibration estimator!");
        }
    }
}
