package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import org.apache.commons.math3.exception.MaxCountExceededException;

public class MultipleLocationsImageFitting implements IEstimator {

    FloatProcessor image;
    Vector<Point> locations;
    double[] subimageData;
    int subimageSize;
    int bigSubImageSize;
    int[] xgrid;
    int[] ygrid;
    Vector<Molecule> results;
    final OneLocationFitter fitter;

    public MultipleLocationsImageFitting(int fittingRadius, OneLocationFitter fitter) {
        this.subimageSize = fittingRadius;
        this.fitter = fitter;
        bigSubImageSize = 2 * fittingRadius + 1;
        subimageData = new double[bigSubImageSize * bigSubImageSize];
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

    public void run() {

        for(int i = 0; i < locations.size(); i++) {
            int xInt = locations.get(i).x.intValue();
            int yInt = locations.get(i).y.intValue();

            if(!isCloseToBorder(xInt, yInt)) {
                try {
                    extractSubimageData(xInt, yInt);
                    //new ImagePlus(String.valueOf(i),new FloatProcessor(2*subimageSize+1, 2*subimageSize+1, subimageData)).show();
                    OneLocationFitter.SubImage subImage = new OneLocationFitter.SubImage(
                            bigSubImageSize,
                            xgrid,
                            ygrid,
                            subimageData,
                            locations.get(i).getX().doubleValue() - xInt,
                            locations.get(i).getY().doubleValue() - yInt);

                    Molecule psf = fitter.fit(subImage);
                    if(psf.isSingleMolecule()) {
                        if(checkIsInSubimage(psf.getX(), psf.getY())) {
                            psf.setX(psf.getX() + xInt + 0.5);
                            psf.setY(psf.getY() + yInt + 0.5);
                            if(!psf.detections.isEmpty()) {
                                psf.detections.clear();
                            }
                            appendCalculatedUncertainty(psf);
                            results.add(psf);
                        }
                    } else {
                        for(Molecule m : psf.detections) {
                            if(checkIsInSubimage(m.getX(), m.getY())) {
                                m.setX(m.getX() + xInt + 0.5);
                                m.setY(m.getY() + yInt + 0.5);
                                appendCalculatedUncertainty(m);
                                results.add(m);
                            }
                        }
                        psf.detections.clear();
                    }
                } catch(MaxCountExceededException ex) {
                    //IJ.log(ex.getMessage());
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
    public Vector<Molecule> estimateParameters(ij.process.FloatProcessor image, Vector<Point> detections) {
        this.image = image;
        this.locations = detections;
        results = new Vector<Molecule>();
        run();
        return extractMacroMolecules(results);
    }

    private boolean checkIsInSubimage(double x, double y) {
        if(Math.abs(x) > subimageSize || Math.abs(y) > subimageSize) {
            return false;
        }
        return true;
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

    private void appendCalculatedUncertainty(Molecule mol) {
        try {
            String paramName;
            double paramValue;
            if(CameraSetupPlugIn.isEmGain) {
                paramName = MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON;
                paramValue = MoleculeDescriptor.Fitting.emccdThompson(mol);
            } else {
                paramName = MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON;
                paramValue = MoleculeDescriptor.Fitting.ccdThompson(mol);
            }
            mol.addParam(paramName, MoleculeDescriptor.Units.NANOMETER, paramValue);
        } catch(Exception e) {
            // ignore...PSF does not fit all the required parameters
        }
    }
}
