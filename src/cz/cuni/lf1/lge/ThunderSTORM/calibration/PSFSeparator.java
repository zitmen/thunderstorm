package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Organizes localizations by position (close detections are grouped together)
 * and by frame.
 *
 */
public class PSFSeparator {

    List<Position> positions = new ArrayList<Position>();
    List<Molecule> allFits = new ArrayList<Molecule>();
    double maxDistance;

    public PSFSeparator(double maxDistance) {
        this.maxDistance = maxDistance * maxDistance; //squared, because i do not do square root when calculating distance
    }

    public synchronized void add(Molecule fit) {
        for(Position p : positions) {
            if(p.getDistanceFromCentroid(fit.getX(), fit.getY()) < maxDistance) {
                p.add(fit);
                allFits.add(fit);
                return;
            }
        }
        Position p = new Position();
        positions.add(p);
        p.add(fit);
        allFits.add(fit);
    }

    public List<Position> getPositions() {
        return positions;
    }
    
    public List<Molecule> getAllFits(){
        return allFits;
    }

    public static class Position {

        double sumX = 0;
        double sumY = 0;
        public double centroidX;
        public double centroidY;
        List<Molecule> fits = new ArrayList<Molecule>();

        private void add(Molecule fit) {
            sumX += fit.getX();
            sumY += fit.getY();
            fits.add(fit);

            centroidX = sumX / fits.size();
            centroidY = sumY / fits.size();
        }

        private double getDistanceFromCentroid(double x, double y) {
            return sqr(x - centroidX) + sqr(y - centroidY);
        }

        public double[] getSigma1AsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getParam(LABEL_SIGMA1);
                i++;
            }
            return array;
        }

        public double[] getSigma2AsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getParam(LABEL_SIGMA2);
                i++;
            }
            return array;
        }

        public double[] getXAsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getX();
                i++;
            }
            return array;
        }

        public double[] getYAsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getY();
                i++;
            }
            return array;
        }

        public double[] getFramesAsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getParam(LABEL_FRAME);
                i++;
            }
            return array;
        }

        public double[] getIntensityAsArray() {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getParam(LABEL_INTENSITY);
                i++;
            }
            return array;
        }

        public int getSize() {
            return fits.size();
        }

        public void discardFitsByFrameRange(double lower, double upper) {
            assert lower <= upper;

            Iterator<Molecule> fitsIterator = fits.iterator();
            while(fitsIterator.hasNext()) {
                int frame = (int) fitsIterator.next().getParam(LABEL_FRAME);
                if(frame < lower || frame > upper) {
                    fitsIterator.remove();
                }
            }
        }
    }

}
