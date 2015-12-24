package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.util.IMatchable;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;

import java.util.*;

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

    public static class Position implements IMatchable<Position> {

        double sumX = 0;
        double sumY = 0;
        public double centroidX;
        public double centroidY;
        List<Molecule> fits = new ArrayList<Molecule>();
        private List<Position> neighbors;   // just for matching

        private void add(Molecule fit) {
            sumX += fit.getX();
            sumY += fit.getY();
            fits.add(fit);

            centroidX = sumX / (double) fits.size();
            centroidY = sumY / (double) fits.size();
        }

        public void recalculateCentroid() {
            sumX = 0.0;
            sumY = 0.0;
            for (Molecule m : fits) {
                sumX += m.getX();
                sumY += m.getY();
            }
            centroidX = sumX / (double) fits.size();
            centroidY = sumY / (double) fits.size();
        }

        private double getDistanceFromCentroid(double x, double y) {
            return sqr(x - centroidX) + sqr(y - centroidY);
        }

        public double[] getAsArray(String fieldName) {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = psf.getParam(fieldName);
                i++;
            }
            return array;
        }

        public void setFromArray(String fieldName, double[] values) {
            if (values.length != fits.size()) {
                throw new IllegalArgumentException("`values` and `fits` must be of the same length!");
            }
            for (int i = 0; i < values.length; i++) {
                fits.get(i).setParam(fieldName, values[i]);
            }
        }

        public double[] getFramesAsArrayOfZ(double z0, double stageStep) {
            double[] array = new double[fits.size()];
            int i = 0;
            for(Molecule psf : fits) {
                array[i] = (psf.getParam(LABEL_FRAME) - z0) * stageStep;
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

        public void discardFitsByFrameSet(Set<Integer> frames) {
            Iterator<Molecule> fitsIterator = fits.iterator();
            while(fitsIterator.hasNext()) {
                int frame = (int) fitsIterator.next().getParam(LABEL_FRAME);
                if(!frames.contains(frame)) {
                    fitsIterator.remove();
                }
            }
        }

        public Set<Integer> getFramesAsSet() {
            Set<Integer> set = new HashSet<Integer>();
            for(Molecule psf : fits) {
                set.add((int) psf.getParam(LABEL_FRAME));
            }
            return set;
        }

        @Override
        public double getX() {
            return centroidX;
        }

        @Override
        public double getY() {
            return centroidY;
        }

        @Override
        public double getZ() {
            return 0.0;
        }

        @Override
        public double getDist2(IMatchable m) {
            return getDistanceFromCentroid(m.getX(), m.getY());
        }

        @Override
        public List<Position> getNeighbors() {
            return neighbors;
        }

        public void addNeighbors(List<Position> nbrs, double dist2thr) {
            if(neighbors == null) {
                neighbors = new ArrayList<Position>();
            }
            for (Position n : nbrs) {
                if (getDist2(n) <= dist2thr) {
                    neighbors.add(n);
                }
            }
        }
    }

}
