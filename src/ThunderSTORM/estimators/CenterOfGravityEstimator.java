package ThunderSTORM.estimators;

import ThunderSTORM.IModule;
import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JPanel;

public class CenterOfGravityEstimator implements IEstimator, IModule {

    @Override
    public String getName() {
        return "Center of Gravity";
    }

    @Override
    public JPanel getOptionsPanel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector<PSF> estimateParameters(FloatProcessor fp, Vector<Point> detections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
