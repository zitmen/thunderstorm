package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public interface ICalibrationProcess {
    void runCalibration();
    DefocusCalibration getCalibration(DefocusFunction defocusModel);
    void drawOverlay();
    void drawSigmaPlots();
}
