package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Morphology;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;

/**
 * Detect pixels with its intensity equal or greater then a threshold and also with its
 * value not changed after a morphological dilation is performed.
 */
public final class NonMaxSuppressionDetector implements IDetector {

    private int radius;
    private String threshold;
    
  public NonMaxSuppressionDetector() throws ThresholdFormulaException {
    this(3, "6*std(F)");
  }
    
    /**
     * Initialize the filter.
     * 
     * @param radius a radius of morphological dilation
     * @param threshold a threshold value
     */
    public NonMaxSuppressionDetector(int radius, String threshold) throws ThresholdFormulaException {
        this.radius = radius;
        this.threshold = threshold;
    }

    /**
     * Detection is performed by applying a grayscale dilation with square uniform kernel
     * of specified radius and then selecting points with their intensity same before and after
     * the dilation and at the same time at least as high as a specified threshold.
     *
     * @param image an input image
     * @return  a {@code Vector} of {@code Points} containing positions of detected molecules
     */
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws ThresholdFormulaException {
        Vector<Point> detections = new Vector<Point>();
        FloatProcessor mx = Morphology.dilateBox(image, radius);
        
        float imval, mxval, thr = Thresholder.getThreshold(threshold);
        for(int x = radius/2, xm = image.getWidth()-radius/2; x < xm; x++) {
            for(int y = radius/2, ym = image.getHeight()-radius/2; y < ym; y++) {
                imval = image.getf(x, y);
                mxval = mx.getf(x, y);
                if((mxval == imval) && (imval >= thr))
                    detections.add(new Point(x, y, imval));
            }
        }
        
        return detections;
    }

}
