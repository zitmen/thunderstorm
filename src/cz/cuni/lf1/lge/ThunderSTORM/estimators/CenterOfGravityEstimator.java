package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * <strong>This is not implemented yet!</strong>
 */
public class CenterOfGravityEstimator implements IEstimator {

    public String getName() {
        return "Center of Gravity";
    }

    public JPanel getOptionsPanel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void readParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
