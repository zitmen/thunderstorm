package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneCalibrationEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.IBinaryTransform;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;

import java.util.Collection;
import java.util.List;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;

public class BiplaneCalibrationProcess extends AbstractCalibrationProcess {

    // processing
    ImagePlus imp1, imp2;
    Roi roi1, roi2;

    public BiplaneCalibrationProcess(IFilterUI selectedFilterUI, IDetectorUI selectedDetectorUI, BiplaneCalibrationEstimatorUI calibrationEstimatorUI,
                                     DefocusFunction defocusModel, double stageStep, double zRangeLimit, ImagePlus imp1, ImagePlus imp2, Roi roi1, Roi roi2) {
        super(selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel, stageStep, zRangeLimit);
        this.imp1 = imp1;
        this.imp2 = imp2;
        this.roi1 = roi1;
        this.roi2 = roi2;
    }

    @Override
    public void runCalibration() {
        Collection<Position> positions = fitPositions();
        IJ.log("Homography transformation matrix: " + transformationMatrix.toString());

        fitQuadraticPolynomials(positions);
        IJ.log("s1 = " + polynomS1Final.toString());
        IJ.log("s2 = " + polynomS2Final.toString());
    }

    @SuppressWarnings("unchecked")
    public DefocusCalibration getCalibration(DefocusFunction defocusModel) {
        DefocusCalibration cal1 = defocusModel.getCalibration(angle, null, polynomS1Final, polynomS1Final);
        DefocusCalibration cal2 = defocusModel.getCalibration(angle, null, polynomS2Final, polynomS2Final);
        return new DoubleDefocusCalibration(cal1.name, transformationMatrix, cal1, cal2);
    }

    public void drawOverlay() {
        // TODO: this doesn't work
        //drawOverlay(imp1, roi1, beadFits1.getAllFits(), usedPositions);
        //drawOverlay(imp2, roi2, beadFits2.getAllFits(), Homography.transformPositions(transformationMatrix,
        //            usedPositions, (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight()));
    }

    protected Collection<Position> fitPositions() {
        angle = 0.0;
        List<Position> fits1 = filterPositions(fitFixedAngle(angle, imp1, roi1, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel));
        List<Position> fits2 = filterPositions(fitFixedAngle(angle, imp2, roi2, selectedFilterUI, selectedDetectorUI, calibrationEstimatorUI, defocusModel));

        IJ.showStatus("Estimating homography between the planes...");
        transformationMatrix = Homography.estimateTransform(
                (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), fits1,
                (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), fits2);
        if (transformationMatrix == null) {
            throw new TransformEstimationFailedException("Could not estimate a transform between the planes!");
        }
        return Homography.mergePositions(transformationMatrix, new IBinaryTransform<Position>() {
                    @Override
                    public void map(Position pos1, Position pos2) {
                        double[] sigma1 = pos1.getAsArray(LABEL_SIGMA, MoleculeDescriptor.Units.PIXEL);
                        double[] sigma2 = pos2.getAsArray(LABEL_SIGMA, MoleculeDescriptor.Units.PIXEL);
                        pos1.setFromArray(LABEL_SIGMA1, MoleculeDescriptor.Units.PIXEL, sigma1);
                        pos1.setFromArray(LABEL_SIGMA2, MoleculeDescriptor.Units.PIXEL, sigma2);
                        pos2.setFromArray(LABEL_SIGMA1, MoleculeDescriptor.Units.PIXEL, sigma1);
                        pos2.setFromArray(LABEL_SIGMA2, MoleculeDescriptor.Units.PIXEL, sigma2);
                    }
                }, (int) roi1.getFloatWidth(), (int) roi1.getFloatHeight(), fits1, (int) roi2.getFloatWidth(), (int) roi2.getFloatHeight(), fits2)
            .keySet();
    }
}
