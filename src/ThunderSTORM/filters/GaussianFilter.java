package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import static ThunderSTORM.utils.Math.gauss;
import ThunderSTORM.utils.Padding;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GaussianFilter extends ConvolutionFilter implements IModule {
    
    private static float [] getKernel(int size, double sigma)
    {
        float [] kernel = new float[size];
        for(int i = 0, center = size/2; i < size; i++) {
            kernel[i] = (float) gauss(i - center, sigma, true);
        }
        return kernel;
    }

    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, Padding.PADDING_DUPLICATE);
    }

    @Override
    public String getName() {
        return "Gaussian blur";
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.add(new JLabel("Size: "), gbc);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JTextField("Size", 20), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel("Sigma: "), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JTextField("Sigma", 20), gbc);
        return panel;
    }
    
}
