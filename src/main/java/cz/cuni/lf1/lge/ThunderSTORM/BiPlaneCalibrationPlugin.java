package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.BiplaneCalibrationDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.BiplaneCalibrationProcess;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.NoMoleculesFittedException;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.TransformEstimationFailedException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.Utils;
import cz.cuni.lf1.lge.ThunderSTORM.util.UI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BiPlaneCalibrationPlugin implements PlugIn {

    DefocusFunction defocusModel;
    IFilterUI selectedFilterUI;
    IDetectorUI selectedDetectorUI;
    BiplaneCalibrationEstimatorUI calibrationEstimatorUI;
    String savePath;
    double stageStep;
    double zRangeLimit;//in nm
    ImagePlus imp1, imp2;
    Roi roi1, roi2;

    private boolean isStack(ImagePlus imp) {
        if(imp == null) {
            IJ.error("No image open.");
            return false;
        }
        if(imp.getImageStackSize() < 2) {
            IJ.error("Requires a stack.");
            return false;
        }
        return true;
    }

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        //
        if (Utils.getOpenImageTitles(true).length < 3) {    // 3 = 2 images + 1 empty string
            IJ.error("Two images must be opened for biplane calibration to work!");
            return;
        }
        //
        try {
            //load modules
            calibrationEstimatorUI = new BiplaneCalibrationEstimatorUI();
            List<IFilterUI> filters = ModuleLoader.getUIModules(IFilterUI.class);
            List<IDetectorUI> detectors = ModuleLoader.getUIModules(IDetectorUI.class);
            List<IEstimatorUI> estimators = Arrays.asList(new IEstimatorUI[]{calibrationEstimatorUI}); // only one estimator can be used
            List<DefocusFunction> defocusFunctions = ModuleLoader.getUIModules(DefocusFunction.class);
            Thresholder.loadFilters(filters);

            // get user options
            try {
                GUI.setLookAndFeel();
            } catch(Exception e) {
                IJ.handleException(e);
            }
            BiplaneCalibrationDialog dialog = new BiplaneCalibrationDialog(filters, detectors, estimators, defocusFunctions);
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
            selectedFilterUI = dialog.getActiveFilterUI();
            selectedDetectorUI = dialog.getActiveDetectorUI();
            savePath = dialog.getSavePath();
            stageStep = dialog.getStageStep();
            zRangeLimit = dialog.getZRangeLimit();
            defocusModel = dialog.getActiveDefocusFunction();

            if (!isStack(imp1 = dialog.getFirstPlaneStack())) return;
            if (!isStack(imp2 = dialog.getSecondPlaneStack())) return;

            roi1 = imp1.getRoi() != null ? imp1.getRoi() : new Roi(0, 0, imp1.getWidth(), imp1.getHeight());
            roi2 = imp2.getRoi() != null ? imp2.getRoi() : new Roi(0, 0, imp2.getWidth(), imp2.getHeight());

            if (roi1.getFloatWidth() != roi2.getFloatWidth() || roi1.getFloatHeight() != roi2.getFloatHeight()) {
                IJ.error("Both used images (or ROIs) must be of the same size!");
                return;
            }

            // perform the calibration
            final BiplaneCalibrationProcess process = new BiplaneCalibrationProcess(
                    selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI,
                    defocusModel, stageStep, zRangeLimit, imp1, imp2, roi1, roi2);

            try {
                process.fitQuadraticPolynomials();
                IJ.log("Homography transformation matrix: " + process.getHomography().toString());
                IJ.log("s1 = " + process.getPolynomS1Final().toString());
                IJ.log("s2 = " + process.getPolynomS2Final().toString());
            } catch(TransformEstimationFailedException ex) {
                IJ.showMessage("Error", ex.getMessage());
                IJ.showStatus("Calibration failed.");
                IJ.showProgress(1.0);
                return;
            } catch(NoMoleculesFittedException ex) {
                // if no beads were successfully fitted, draw localizations anyway
                IJ.log("Homography transformation matrix: " + process.getHomography().toString());
                process.drawOverlay();
                IJ.showMessage("Error", ex.getMessage());
                IJ.showStatus("Calibration failed.");
                IJ.showProgress(1.0);
                return;
            }
            process.drawOverlay();
            process.drawSigmaPlots();

            try {
                process.getCalibration(defocusModel).saveToFile(savePath);
            } catch(IOException ex) {
                UI.showAnotherLocationDialog(ex, process.getCalibration(defocusModel));
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }
}
