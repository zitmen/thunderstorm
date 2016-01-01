package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.UncertaintyNotApplicableException;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.MeasurementProtocol;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;

import java.util.ArrayList;
import java.util.List;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.uncertaintyXY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Fitting.uncertaintyZ;

public class MultipleLocationsImageFitting implements IEstimator {

    FloatProcessor image;
    List<Point> locations;
    double[] subimageData;
    int subimageSize;
    int bigSubImageSize;
    double[] xgrid;
    double[] ygrid;
    List<Molecule> results;
    final IOneLocationFitter fitter;
    MoleculeDescriptor moleculeDescriptor;

    public MultipleLocationsImageFitting(int fittingRadius, IOneLocationFitter fitter) {
        this.subimageSize = fittingRadius;
        this.fitter = fitter;
        bigSubImageSize = 2 * fittingRadius + 1;
        subimageData = new double[bigSubImageSize * bigSubImageSize];
        initializeGrid();
    }

    private void initializeGrid() {
        xgrid = new double[bigSubImageSize * bigSubImageSize];
        ygrid = new double[bigSubImageSize * bigSubImageSize];

        int idx = 0;
        for(int i = -subimageSize; i <= subimageSize; i++) {
            for(int j = -subimageSize; j <= subimageSize; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }
    }

    public void extractSubimageData(int x, int y) {
        float[] pixels = (float[]) image.getPixels();
        int roiX = x - subimageSize;
        int roiY = y - subimageSize;

        for(int ys = roiY; ys < roiY + bigSubImageSize; ys++) {
            int offset1 = (ys - roiY) * bigSubImageSize;
            int offset2 = ys * image.getWidth() + roiX;
            for(int xs = 0; xs < bigSubImageSize; xs++) {
                subimageData[offset1++] = pixels[offset2++];
            }
        }
    }

    public void run() throws StoppedByUserException {

        for(int i = 0; i < locations.size(); i++) {
            GUI.checkIJEscapePressed();
            int xInt = locations.get(i).x.intValue();
            int yInt = locations.get(i).y.intValue();

            if(!isCloseToBorder(xInt, yInt)) {
                try {
                    extractSubimageData(xInt, yInt);
                    //new ImagePlus(String.valueOf(i),new FloatProcessor(2*subimageSize+1, 2*subimageSize+1, subimageData)).show();
                    SubImage subImage = new SubImage(
                            2*subimageSize+1,
                            2*subimageSize+1,
                            xgrid,
                            ygrid,
                            subimageData,
                            locations.get(i).getX().doubleValue() - xInt,
                            locations.get(i).getY().doubleValue() - yInt);

                    Molecule psf = fitter.fit(subImage);
                    //replace molecule descriptor to a common one for all molecules
                    if(moleculeDescriptor != null){
                        moleculeDescriptor.validateMolecule(psf);
                        psf.descriptor = moleculeDescriptor;
                    }else{
                        moleculeDescriptor = psf.descriptor;
                    }
                    if(psf.isSingleMolecule()) {
                        if(checkIsInSubimage(psf.getX(), psf.getY())) {
                            psf.setX(psf.getX() + xInt + 0.5);
                            psf.setY(psf.getY() + yInt + 0.5);
                            psf.setDetections(null);
                            appendGoodnessOfFit(psf, fitter, subImage);
                            appendCalculatedUncertainty(psf);
                            results.add(psf);
                        }
                    } else {
                        for(Molecule m : psf.getDetections()) {
                            if(checkIsInSubimage(m.getX(), m.getY())) {
                                m.setX(m.getX() + xInt + 0.5);
                                m.setY(m.getY() + yInt + 0.5);
                                appendGoodnessOfFit(m, fitter, subImage);
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

    boolean isCloseToBorder(int x, int y) {
        if(x < subimageSize || x > image.getWidth() - subimageSize - 1) {
            return true;
        }
        if(y < subimageSize || y > image.getHeight() - subimageSize - 1) {
            return true;
        }
        return false;
    }

    @Override
    public List<Molecule> estimateParameters(ij.process.FloatProcessor image, List<Point> detections) throws StoppedByUserException{
        this.image = image;
        this.locations = detections;
        results = new ArrayList<Molecule>();
        run();
        return extractMacroMolecules(results);
    }

    private boolean checkIsInSubimage(double x, double y) {
        if(Math.abs(x) > subimageSize || Math.abs(y) > subimageSize) {
            return false;
        }
        return true;
    }

    private List<Molecule> extractMacroMolecules(List<Molecule> macroMolecules) {
        List<Molecule> extracted = new ArrayList<Molecule>();
        for(Molecule mol : macroMolecules) {
            if(mol.isSingleMolecule()) {
                extracted.add(mol);
            } else {
                extracted.addAll(mol.getDetections());
            }
        }
        return extracted;
    }

    public static void appendGoodnessOfFit(Molecule mol, IOneLocationFitter fitter, SubImage subimage) {
        LSQFitter lsqfit;
        if(fitter instanceof LSQFitter) {
            lsqfit = (LSQFitter)fitter;
        } else if(fitter instanceof MFA_LSQFitter) {
            lsqfit = ((MFA_LSQFitter)fitter).lastFitter;
        } else {
            return;
        }
        double chi2 = lsqfit.psfModel.getChiSquared(subimage.xgrid, subimage.ygrid, subimage.values, lsqfit.fittedParameters, lsqfit.useWeighting);
        mol.addParam(MoleculeDescriptor.Fitting.LABEL_CHI2, MoleculeDescriptor.Units.UNITLESS, chi2);
    }

    public static void appendCalculatedUncertainty(Molecule mol) {
        try {
            MeasurementProtocol protocol = IJResultsTable.getResultsTable().getMeasurementProtocol();
            if (protocol != null) {
                mol.addParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, MoleculeDescriptor.Units.NANOMETER, uncertaintyXY(mol));
                if (protocol.is3D()) {
                    mol.addParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z, MoleculeDescriptor.Units.NANOMETER, uncertaintyZ(mol));
                }
            }
        } catch (UncertaintyNotApplicableException ex) {
            IJ.log("\\Update:Cannot calculate fitting uncertainty: " + ex.getMessage());
        } catch (NullPointerException ex) {
            IJ.log("\\Update:Measurement protocol wasn't set properly to calculate uncertainty!");
        }
    }
}
