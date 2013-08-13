package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import java.util.ArrayList;
import java.util.List;

/**
 * Organizes localizations by position (close molecules are grouped together)
 * and by frame.
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class PSFSeparator {

  List<Position> positions = new ArrayList<Position>();
  double maxDistance;

  public PSFSeparator(double maxDistance) {
    this.maxDistance = maxDistance * maxDistance; //squared, because i do not do square root when calculating distance
  }

  public synchronized void add(Molecule fit, int frame) {
    for (Position p : positions) {
      if (p.getDistanceFromCentroid(fit.getX(), fit.getY()) < maxDistance) {
        p.add(fit, frame);
        return;
      }
    }
    Position p = new Position();
    positions.add(p);
    p.add(fit, frame);
  }

  public List<Position> getPositions() {
    return positions;
  }

  public static class Position {

    double sumX = 0;
    double sumY = 0;
    public double centroidX;
    public double centroidY;
    List<Integer> frames  = new ArrayList<Integer>();
    List<Molecule> fits = new ArrayList<Molecule>();

    private void add(Molecule fit, int frame) {
      sumX += fit.getX();
      sumY += fit.getY();
      frames.add(frame);
      fits.add(fit);

      centroidX = sumX / frames.size();
      centroidY = sumY / frames.size();
    }

    private double getDistanceFromCentroid(double x, double y) {
      return sqr(x - centroidX) + sqr(y - centroidY);
    }

    public double[] getSigma1AsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (Molecule psf : fits) {
        array[i] = psf.getParam(LABEL_SIGMA1);
        i++;
      }
      return array;
    }

    public double[] getSigma2AsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (Molecule psf : fits) {
        array[i] = psf.getParam(LABEL_SIGMA2);
        i++;
      }
      return array;
    }

    public double[] getXAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (Molecule psf : fits) {
        array[i] = psf.getX();
        i++;
      }
      return array;
    }
    
    public double[] getYAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (Molecule psf : fits) {
        array[i] = psf.getY();
        i++;
      }
      return array;
    }

    public double[] getFramesAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (Integer frame : frames) {
        array[i] = frame;
        i++;
      }
      return array;
    }
  }
}
