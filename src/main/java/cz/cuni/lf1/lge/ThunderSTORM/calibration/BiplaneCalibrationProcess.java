package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import java.util.Collection;

public class BiplaneCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp1, imp2;
    Roi roi1, roi2;

    // results
    private Homography.TransformationMatrix transformationMatrix;
    private PSFSeparator beadFits1, beadFits2;

    public BiplaneCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, BiplaneCalibrationEstimatorUI calibrationEstimatorUI, DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2) {
        super(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp1 = imp1;
        this.imp2 = imp2;
        this.roi1 = roi1;
        this.roi2 = roi2;
    }

    @Override
    protected Collection<Position> fitPositions(double angle) {
        beadFits1 = fitFixedAngle(angle, imp1, roi1, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);
        beadFits2 = fitFixedAngle(angle, imp2, roi2, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel);

        IJ.showStatus("Estimating homography between the planes...");
        transformationMatrix = Homography.estimateTransform(
                (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), beadFits1,
                (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), beadFits2);
        if (transformationMatrix == null) {
            throw new TransformEstimationFailedException("Could not estimate a transform between the planes!");
        }
        return Homography.mergePositions(transformationMatrix,
                (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), beadFits1,
                (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), beadFits2);
    }

    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        return defocusModel.getCalibration(transformationMatrix, polynomS1Final, polynomS2Final);
    }

    public Homography.TransformationMatrix getHomography() {
        return transformationMatrix;
    }

    public void drawOverlay() {
        drawOverlay(imp1, roi1, beadFits1.getAllFits(), usedPositions);
        drawOverlay(imp2, roi2, beadFits2.getAllFits(), Homography.transformPositions(transformationMatrix,
                    usedPositions, (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight()));
    }
}
