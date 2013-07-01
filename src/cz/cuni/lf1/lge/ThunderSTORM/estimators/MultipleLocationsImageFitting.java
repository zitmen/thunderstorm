package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.util.Vector;
import org.apache.commons.math3.exception.MaxCountExceededException;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class MultipleLocationsImageFitting implements IEstimator {

  FloatProcessor image;
  Vector<Point> locations;
  double[] subimageData;
  int subimageSize;
  int bigSubImageSize;
  int[] xgrid;
  int[] ygrid;
  Vector<PSFInstance> results;
  final OneLocationFitter fitter;

  public MultipleLocationsImageFitting(int subimageSize, OneLocationFitter fitter) {
    this.subimageSize = subimageSize;
    this.fitter = fitter;
    bigSubImageSize = 2 * subimageSize + 1;
    subimageData = new double[bigSubImageSize * bigSubImageSize];
    initializeGrid();
  }

  private void initializeGrid() {
    xgrid = new int[bigSubImageSize * bigSubImageSize];
    ygrid = new int[bigSubImageSize * bigSubImageSize];

    int idx = 0;
    for (int i = -subimageSize; i <= subimageSize; i++) {
      for (int j = -subimageSize; j <= subimageSize; j++) {
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

    for (int ys = roiY; ys < roiY + bigSubImageSize; ys++) {
      int offset1 = (ys - roiY) * bigSubImageSize;
      int offset2 = ys * image.getWidth() + roiX;
      for (int xs = 0; xs < bigSubImageSize; xs++) {
        subimageData[offset1++] = pixels[offset2++];
      }
    }
  }

  public void run() {

    for (int i = 0; i < locations.size(); i++) {
      int xInt = locations.get(i).x.intValue();
      int yInt = locations.get(i).y.intValue();

      if (!isCloseToBorder(xInt, yInt)) {
        try {
          extractSubimageData(xInt, yInt);
          //new ImagePlus(String.valueOf(i),new FloatProcessor(2*subimageSize+1, 2*subimageSize+1, subimageData)).show();
          OneLocationFitter.SubImage subImage = new OneLocationFitter.SubImage(
                  xgrid,
                  ygrid,
                  subimageData,
                  locations.get(i).getX().doubleValue() - xInt,
                  locations.get(i).getY().doubleValue() - yInt);

          PSFInstance psf = fitter.fit(subImage);
          if (checkIsInSubimage(psf.getX(), psf.getY())) {
            psf.setX(psf.getX() + xInt + 0.5);
            psf.setY(psf.getY() + yInt + 0.5);
            results.add(psf);
          }
        } catch (MaxCountExceededException ex) {
          //IJ.log(ex.getMessage());
        }
      }
    }
  }

  boolean isCloseToBorder(int x, int y) {
    if (x < subimageSize || x > image.getWidth() - subimageSize - 1) {
      return true;
    }
    if (y < subimageSize || y > image.getHeight() - subimageSize - 1) {
      return true;
    }
    return false;
  }

  @Override
  public Vector<PSFInstance> estimateParameters(ij.process.FloatProcessor image, Vector<Point> detections) {
    this.image = image;
    this.locations = detections;
    results = new Vector<PSFInstance>();
    run();
    return results;
  }

  private boolean checkIsInSubimage(double x, double y) {
    if (Math.abs(x) > subimageSize || Math.abs(y) > subimageSize) {
      return false;
    }
    return true;
  }
}
