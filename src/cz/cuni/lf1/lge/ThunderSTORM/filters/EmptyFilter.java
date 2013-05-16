package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.process.FloatProcessor;
import javax.swing.JPanel;

/**
 * No filtering.
 * 
 * This is useful in case of detectors of estimators that work better with raw images.
 * The {@code filterImage} method returns the {@code image} that it got on its input.
 */
public final class EmptyFilter implements IFilter {

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
