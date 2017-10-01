package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.PhasorAstigmatismCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

public class PhasorAstigmaticCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp;
    Roi roi;

    // results
    private PSFSeparator beadFits;

    public PhasorAstigmaticCalibrationProcess(CalibrationConfig config, IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI,
                                        PhasorAstigmatismCalibrationEstimatorUI calibrationEstimatorUI, DefocusFunction defocusModel,
                                        double stageStep, double zRangeLimit, ImagePlus imp, Roi roi, boolean z0InMiddleOfStack) {
        super(config, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit, z0InMiddleOfStack);
        this.imp = imp;
        this.roi = roi;
    }

    @Override
    public void runCalibration() {
        //Adding 0.001 to escape angle=0 errors
        angle = estimateAngle(imp, roi);//+0.0927;
        
        beadFits = fitFixedAngle(angle, imp, roi, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, config.showResultsTable);
        fitQuadraticPolynomials(imp, beadFits.getPositions());
        //To get fitradius: calibrationEstimatorUI.getFitradius()
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
