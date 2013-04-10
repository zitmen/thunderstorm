package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import ij.process.FloatProcessor;
import javax.swing.JPanel;

public final class EmptyFilter implements IFilter, IModule {

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return image;
    }

    @Override
    public String getName() {
        return "No filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public void readParameters() {
        // nothing to do here
    }
    
}
