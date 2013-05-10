package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JPanel;

/**
 *
 */
public class EmptyEstimator implements IEstimator, IModule {

    /**
     *
     * @param fp
     * @param detections
     * @return
     */
    @Override
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        Vector<PSF> locations = new Vector<PSF>();
        for(Point detection : detections) {
            locations.add(new GaussianPSF(detection.x.doubleValue(), detection.y.doubleValue()));
        }
        return locations;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "No estimator";
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    /**
     *
     */
    @Override
    public void readParameters() {
        // nothing to do here
    }
    
}
