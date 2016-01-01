package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * This is used for MFA PSF fitting on the entire image.
 */
public class FullImageFitting implements IEstimator {

    IOneLocationFitter fitter;
    double[] xgrid;
    double[] ygrid;

    public FullImageFitting(IOneLocationFitter fitter) {
        this.fitter = fitter;
    }
    
    @Override
    public List<Molecule> estimateParameters(FloatProcessor image, List<Point> detections) throws StoppedByUserException {
        List<Molecule> results = new ArrayList<Molecule>();
        try {
            int w = image.getWidth();
            int h = image.getHeight();
            int x0 = w / 2;
            int y0 = h / 2;
            initializeGrid(x0, y0, w, h);
            int maxI = getBestDetection(detections);
            SubImage subImage = new SubImage(
                    image.getWidth(), image.getHeight(),
                    xgrid, ygrid, getImageData(image),
                    detections.get(maxI).x.doubleValue() - x0,
                    detections.get(maxI).y.doubleValue() - y0);

            Molecule psf = fitter.fit(subImage);
            if(psf.isSingleMolecule()) {
                if(checkIsInSubimage(psf.getX(), psf.getY(), image.getWidth(), image.getHeight())) {
                    psf.setX(psf.getX() + x0 + 0.5);
                    psf.setY(psf.getY() + y0 + 0.5);
                    psf.setDetections(null);
                    MultipleLocationsImageFitting.appendGoodnessOfFit(psf, fitter, subImage);
                    MultipleLocationsImageFitting.appendCalculatedUncertainty(psf);
                    results.add(psf);
                }
            } else {
                for(Molecule m : psf.getDetections()) {
                    if(checkIsInSubimage(m.getX(), m.getY(), image.getWidth(), image.getHeight())) {
                        m.setX(m.getX() + x0 + 0.5);
                        m.setY(m.getY() + y0 + 0.5);
                        MultipleLocationsImageFitting.appendGoodnessOfFit(m, fitter, subImage);
                        MultipleLocationsImageFitting.appendCalculatedUncertainty(m);
                        results.add(m);
                    }
                }
                psf.setDetections(null);
            }
        } catch(Exception ex) {
            //
        }
        return results;
    }
    
    private void initializeGrid(int x0, int y0, int w, int h) {
        xgrid = new double[w * h];
        ygrid = new double[w * h];
        
        int idx = 0;
        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                xgrid[idx] = x - x0;
                ygrid[idx] = y - y0;
                idx++;
            }
        }
    }
    
    private boolean checkIsInSubimage(double x, double y, double w, double h) {
        return Math.abs(x) <= w && Math.abs(y) <= h;
    }
    
    private double [] getImageData(FloatProcessor img) {
        float[] pixels = (float[]) img.getPixels();
        double[] imgData = new double[pixels.length];
        for(int i = 0; i < pixels.length; i++) {
            imgData[i] = (double)pixels[i];
        }
        return imgData;
    }

    private int getBestDetection(List<Point> detections) {
        int maxI = 0;
        for(int i = 1, im = detections.size(); i < im; i++) {
            if(detections.get(i).val.doubleValue() > detections.get(maxI).val.doubleValue()) {
                maxI = i;
            }
        }
        return maxI;
    }

}
