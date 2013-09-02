package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.CentroidFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import javax.swing.JPanel;

public class CenterOfMassEstimatorUI extends IEstimatorUI {

    private final String name = "Center of Gravity";
    private final transient int fitradius = 1;  // square 3x3
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        return new JPanel();
    }

    @Override
    public void readParameters() {
        //
    }

    @Override
    public void resetToDefaults() {
        //
    }

    @Override
    public void recordOptions() {
        //
    }

    @Override
    public void readMacroOptions(String options) {
        //
    }

    @Override
    public IEstimator getImplementation() {
        return new MultipleLocationsImageFitting(fitradius, new CentroidFitter());
    }
    
}
