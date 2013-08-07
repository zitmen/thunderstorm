package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import java.util.List;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import java.util.ArrayList;

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

  public synchronized void add(PSFInstance fit, int frame) {
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
    List<PSFInstance> fits = new ArrayList<PSFInstance>();

    private void add(PSFInstance fit, int frame) {
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

    public double[] getSigmaAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (PSFInstance psf : fits) {
        array[i] = psf.getParam(PSFInstance.SIGMA);
        i++;
      }
      return array;
    }

    public double[] getSigma2AsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (PSFInstance psf : fits) {
        array[i] = psf.getParam(PSFInstance.SIGMA2);
        i++;
      }
      return array;
    }

    public double[] getXAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (PSFInstance psf : fits) {
        array[i] = psf.getX();
        i++;
      }
      return array;
    }
    
    public double[] getYAsArray() {
      double[] array = new double[frames.size()];
      int i = 0;
      for (PSFInstance psf : fits) {
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
