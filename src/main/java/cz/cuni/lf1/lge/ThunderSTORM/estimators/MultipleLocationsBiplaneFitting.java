package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.Homography;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.UncertaintyNotApplicableException;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.MeasurementProtocol;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;

import java.util.List;
import java.util.Vector;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.uncertaintyXY;

public class MultipleLocationsBiplaneFitting implements IBiplaneEstimator {

    FloatProcessor plane1, plane2;
    List<Pair<Point, Point>> locations;
    double distThrNm;
    double[] subimageData1;
    double[] subimageData2;
    int subimageSize;
    int bigSubImageSize;
    int[] xgrid;
    int[] ygrid;
    Vector<Molecule> results;
    final OneLocationBiplaneFitter fitter;
    MoleculeDescriptor moleculeDescriptor;
    Homography.TransformationMatrix homography;

    public MultipleLocationsBiplaneFitting(int fittingRadius, double distThrNm, Homography.TransformationMatrix homography, OneLocationBiplaneFitter fitter) {
        this.subimageSize = fittingRadius;
        this.distThrNm = distThrNm;
        this.homography = homography;
        this.fitter = fitter;
        bigSubImageSize = 2 * fittingRadius + 1;
        subimageData1 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subimageData2 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        initializeGrid();
    }

    private void initializeGrid() {
        xgrid = new int[bigSubImageSize * bigSubImageSize];
        ygrid = new int[bigSubImageSize * bigSubImageSize];

        int idx = 0;
        for(int i = -subimageSize; i <= subimageSize; i++) {
            for(int j = -subimageSize; j <= subimageSize; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }
    }

    public void extractSubimageData(double[] /*[out]*/data, FloatProcessor image, int x, int y) {
        float[] pixels = (float[]) image.getPixels();
        int roiX = x - subimageSize;
        int roiY = y - subimageSize;

        for(int ys = roiY; ys < roiY + bigSubImageSize; ys++) {
            int offset1 = (ys - roiY) * bigSubImageSize;
            int offset2 = ys * image.getWidth() + roiX;
            for(int xs = 0; xs < bigSubImageSize; xs++) {
                data[offset1++] = pixels[offset2++];
            }
        }
    }

    public void run() throws StoppedByUserException {

        for (Pair<Point, Point> location : locations) {
            GUI.checkIJEscapePressed();
            int xInt1 = location.first.x.intValue();
            int yInt1 = location.first.y.intValue();
            int xInt2 = location.second.x.intValue();
            int yInt2 = location.second.y.intValue();

            if (!isCloseToBorder(plane1, xInt1, yInt1) && !isCloseToBorder(plane2, xInt2, yInt2)) {
                try {
                    extractSubimageData(subimageData1, plane1, xInt1, yInt1);
                    extractSubimageData(subimageData2, plane2, xInt2, yInt2);
                    SubImage subImage1 = new SubImage(
                            2 * subimageSize + 1, 2 * subimageSize + 1,
                            xgrid, ygrid, subimageData1,
                            location.first.getX().doubleValue() - xInt1,
                            location.first.getY().doubleValue() - yInt1);
                    SubImage subImage2 = new SubImage(
                            2 * subimageSize + 1, 2 * subimageSize + 1,
                            xgrid, ygrid, subimageData2,
                            location.second.getX().doubleValue() - xInt2,
                            location.second.getY().doubleValue() - yInt2);

                    Molecule psf = fitter.fit(subImage1, subImage2);
                    //replace molecule descriptor to a common one for all molecules
                    if (moleculeDescriptor != null) {
                        moleculeDescriptor.validateMolecule(psf);
                        psf.descriptor = moleculeDescriptor;
                    } else {
                        moleculeDescriptor = psf.descriptor;
                    }
                    if (psf.isSingleMolecule()) {
                        if (checkIsInSubimage(psf.getX(), psf.getY())) {
                            psf.setX(psf.getX() + xInt1 + 0.5); // x-position in the first plane
                            psf.setY(psf.getY() + yInt1 + 0.5); // y-position in the first plane
                            psf.setDetections(null);
                            appendGoodnessOfFit(psf, fitter, subImage1, subImage2);
                            appendCalculatedUncertainty(psf);
                            results.add(psf);
                        }
                    } else {
                        for (Molecule m : psf.getDetections()) {
                            if (checkIsInSubimage(m.getX(), m.getY())) {
                                m.setX(m.getX() + xInt1 + 0.5); // x-position in the first plane
                                m.setY(m.getY() + yInt1 + 0.5); // y-position in the first plane
                                appendGoodnessOfFit(m, fitter, subImage1, subImage2);
                                appendCalculatedUncertainty(m);
                                results.add(m);
                            }
                        }
                        psf.setDetections(null);
                    }
                } catch (MaxCountExceededException ex) {
                    // maximum number of iterations has been exceeded (it is set very high, so it usually means trouble)
                    IJ.log("Warning: the fitter couldn't converge (max. count of iterations was exceeded)! The molecule candidate has been thrown away.");
                } catch (ConvergenceException ex) {
                    // exception: "org.apache.commons.math3.exception.ConvergenceException: illegal state:
                    //             unable to perform Q.R decomposition on the 49x11 jacobian matrix"
                    // -> probably NaN or Inf value in one of the estimated parameter
                    //    or during the evaluation of PSF model or ots derivative
                    // -> another possible reason is that camera offset is set too high, which in combination with WLSQ
                    //    fitting may lead to division by zero, since intensities are used for weighting!
                    IJ.log("Warning: the fitter couldn't converge (probably NaN or Inf occurred in calculations; if you use WLSQ fitting, check if camera offset isn't too high correctly; if not try MLE or LSQ fitting)! The molecule candidate has been thrown away.");
                }
            }
        }
    }

    boolean isCloseToBorder(FloatProcessor image, int x, int y) {
        return x < subimageSize || x > image.getWidth() - subimageSize - 1
            || y < subimageSize || y > image.getHeight() - subimageSize - 1;
    }

    @Override
    public Vector<Molecule> estimateParameters(FloatProcessor plane1, FloatProcessor plane2,
                                               List<Point> detections1, List<Point> detections2) throws StoppedByUserException{
        this.plane1 = plane1;
        this.plane2 = plane2;
        this.locations = Homography.mergePositions(plane1.getWidth(), plane2.getHeight(),
                                    homography, detections1, detections2, distThrNm*distThrNm);
        results = new Vector<Molecule>();
        run();
        return extractMacroMolecules(results);
    }

    private boolean checkIsInSubimage(double x, double y) {
        return !(Math.abs(x) > subimageSize || Math.abs(y) > subimageSize);
    }

    private Vector<Molecule> extractMacroMolecules(Vector<Molecule> macroMolecules) {
        Vector<Molecule> extracted = new Vector<Molecule>();
        for(Molecule mol : macroMolecules) {
            if(mol.isSingleMolecule()) {
                extracted.add(mol);
            } else {
                extracted.addAll(mol.getDetections());
            }
        }
        return extracted;
    }

    public static void appendGoodnessOfFit(Molecule mol, OneLocationBiplaneFitter fitter, SubImage subimage1, SubImage subimage2) {
        // TODO: implement chi2 calculation!!!
        IJ.log("\\Update:Method `appendGoodnessOfFit`: not implemented!");
        /*
        LSQFitter lsqfit;
        if(fitter instanceof LSQFitter) {
            lsqfit = (LSQFitter)fitter;
        } else if(fitter instanceof MFA_LSQFitter) {
            lsqfit = ((MFA_LSQFitter)fitter).lastFitter;
        } else {
            return;
        }
        double chi2 = lsqfit.psfModel.getChiSquared(subimage1.xgrid, subimage1.ygrid, subimage1.values,
                                                    subimage2.xgrid, subimage2.ygrid, subimage2.values,
                                                    lsqfit.fittedParameters, lsqfit.useWeighting);
        mol.addParam(MoleculeDescriptor.Fitting.LABEL_CHI2, MoleculeDescriptor.Units.UNITLESS, chi2);
        */
    }

    public static void appendCalculatedUncertainty(Molecule mol) {
        try {
            MeasurementProtocol protocol = IJResultsTable.getResultsTable().getMeasurementProtocol();
            if (protocol != null) {
                mol.addParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, MoleculeDescriptor.Units.NANOMETER, uncertaintyXY(mol));
            }
        } catch (UncertaintyNotApplicableException ex) {
            IJ.log("\\Update:Cannot calculate fitting uncertainty: " + ex.getMessage());
        } catch (NullPointerException ex) {
            IJ.log("\\Update:Measurement protocol wasn't set properly to calculate uncertainty!");
        }
    }
}
