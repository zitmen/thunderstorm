package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import ij.process.FloatProcessor;
import javax.swing.JPanel;

/**
 *
 */
public final class EmptyFilter implements IFilter, IModule {

    /**
     *
     * @param image
     * @return
     */
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return image;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "No filter";
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
