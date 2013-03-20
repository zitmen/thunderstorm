package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ij.process.FloatProcessor;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EmptyFilter implements IFilter, IModule {

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
    
}
