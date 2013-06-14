package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import java.util.List;
import java.util.Map;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import java.util.ArrayList;
import java.util.HashMap;

/**
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
    double centroidX;
    double centroidY;
    Map<Integer, PSFInstance> fitsByFrame = new HashMap<Integer, PSFInstance>();

    private void add(PSFInstance fit, int frame) {
      sumX += fit.getX();
      sumY += fit.getY();
      fitsByFrame.put(frame, fit);

      centroidX = sumX / fitsByFrame.size();
      centroidY = sumY / fitsByFrame.size();
    }

    private double getDistanceFromCentroid(double x, double y) {
      return sqr(x - centroidX) + sqr(y - centroidY);
    }

    public double[] getSigmaAsArray() {
      double[] array = new double[fitsByFrame.size()];
      int i = 0;
      for (Map.Entry<Integer, PSFInstance> entry : fitsByFrame.entrySet()) {
        array[i] = entry.getValue().getParam(PSFInstance.SIGMA);
        i++;
      }
      return array;
    }
    public double[] getSigma2AsArray() {
      double[] array = new double[fitsByFrame.size()];
      int i = 0;
      for (Map.Entry<Integer, PSFInstance> entry : fitsByFrame.entrySet()) {
        array[i] = entry.getValue().getParam(PSFInstance.SIGMA2);
        i++;
      }
      return array;
    }
    public double[] getFramesAsArray(){
      double[] array = new double[fitsByFrame.size()];
      int i = 0;
      for (Integer val : fitsByFrame.keySet()) {
        array[i] = val;
        i++;
      }
      return array;
    }
  }
}
