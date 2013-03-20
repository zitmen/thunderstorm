package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.GridBagHelper;
import ThunderSTORM.utils.ImageProcessor;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// This filter uses a trick to be effective:
// - convolution with a non-separable kernel has a computational complexity K*K*N
// - this approach uses two separable convolutions and then calculating their difference, i.e., 2*(2*K*N)+N, which is asymtotically faster!
// However there is a question of how much is the performance degraded due to the memory allocation of 2 images instead of 1.
public class DifferenceOfGaussiansFilter implements IFilter, IModule {

    private boolean params_changed;
    private int size;
    private double sigma_g1, sigma_g2;
    
    private JTextField sigma1TextField, sigma2TextField, sizeTextField;
    
    private GaussianFilter g1, g2;
    
    private void initialize() {
        g1 = new GaussianFilter(size, sigma_g1);
        g2 = new GaussianFilter(size, sigma_g2);
        params_changed = false;
    }
    
    public DifferenceOfGaussiansFilter(int size, double sigma_g1, double sigma_g2) {
        this.size = size;
        this.sigma_g1 = sigma_g1;
        this.sigma_g2 = sigma_g2;
        params_changed = true;
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        if(params_changed) initialize();
        return ImageProcessor.subtractImage(g1.filterImage(image), g2.filterImage(image));
    }

    @Override
    public String getName() {
        return "Box filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Size: "), GridBagHelper.pos(0,0));
        panel.add(new JTextField(Integer.toString(size), 20), GridBagHelper.pos(1,0));
        panel.add(new JLabel("Sigma1: "), GridBagHelper.pos(0,1));
        panel.add(new JTextField(Double.toString(sigma_g1), 20), GridBagHelper.pos(1,1));
        panel.add(new JLabel("Sigma2: "), GridBagHelper.pos(0,2));
        panel.add(new JTextField(Double.toString(sigma_g2), 20), GridBagHelper.pos(1,2));
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            size = Integer.parseInt(sizeTextField.getText());
            sigma_g1 = Double.parseDouble(sigma1TextField.getText());
            sigma_g2 = Double.parseDouble(sigma2TextField.getText());
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
