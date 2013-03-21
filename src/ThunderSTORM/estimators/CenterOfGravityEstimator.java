package ThunderSTORM.estimators;

import ThunderSTORM.IModule;
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
    
}
