package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import static ThunderSTORM.utils.Math.mean;
import ThunderSTORM.utils.ImageProcessor;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// This filter uses the same trick to be effective as the DoG filter
public class LoweredGaussianFilter implements IFilter, IModule {
    
    private GaussianFilter g;
    private UniformFilter u;
    
    public LoweredGaussianFilter(int size, double sigma) {
        g = new GaussianFilter(size, sigma);
        u = new UniformFilter(size, mean((float []) g.getKernelX().getPixels()));
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return ImageProcessor.subtractImage(g.filterImage(image), u.filterImage(image));
    }

    @Override
    public String getName() {
        return "Lowered Gaussian filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Size: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Size", 20), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Sigma: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Sigma", 20), gbc);
        return panel;
    }
    
}
