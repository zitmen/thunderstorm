package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.ImageProcessor;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// This filter uses a trick to be effective:
// - convolution with a non-separable kernel has a computational complexity K*K*N
// - this approach uses two separable convolutions and then calculating their difference, i.e., 2*(2*K*N)+N, which is asymtotically faster!
// However there is a question of how much is the performance degraded due to the memory allocation of 2 images instead of 1.
public class DifferenceOfGaussiansFilter implements IFilter, IModule {

    private GaussianFilter g1;
    private GaussianFilter g2;
    
    public DifferenceOfGaussiansFilter(int size, double sigma_g1, double sigma_g2) {
        g1 = new GaussianFilter(size, sigma_g1);
        g2 = new GaussianFilter(size, sigma_g2);
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return ImageProcessor.subtractImage(g1.filterImage(image), g2.filterImage(image));
    }

    @Override
    public String getName() {
        return "Box filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridx = 0;
        panel.add(new JLabel("Size: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Size", 20), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Sigma1: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Sigma1", 20), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Sigma2: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Sigma2", 20), gbc);
        return panel;
    }
    
}
