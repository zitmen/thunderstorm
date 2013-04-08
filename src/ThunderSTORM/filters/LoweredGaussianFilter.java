package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.GridBagHelper;
import static ThunderSTORM.utils.Math.mean;
import ThunderSTORM.utils.ImageProcessor;
import ThunderSTORM.utils.Padding;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// This filter uses the same trick to be effective as the DoG filter
public final class LoweredGaussianFilter implements IFilter, IModule {
    
    private GaussianFilter g;
    private UniformFilter u;
    
    private int size, padding;
    private double sigma;
    
    private JTextField sizeTextField, sigmaTextField;
    
    private void updateKernel() {
        g = new GaussianFilter(size, sigma, padding);
        u = new UniformFilter(size, mean((float []) g.getKernelX().getPixels()), padding);
    }
    
    public LoweredGaussianFilter(int size, double sigma) {
        this.size = size;
        this.sigma = sigma;
        this.padding = Padding.PADDING_DUPLICATE;
        updateKernel();
    }
    
    public LoweredGaussianFilter(int size, double sigma, int padding_method) {
        this.size = size;
        this.sigma = sigma;
        this.padding = padding_method;
        updateKernel();
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
        sizeTextField = new JTextField(Integer.toString(size), 20);
        sigmaTextField = new JTextField(Double.toString(sigma), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Size: "), GridBagHelper.pos(0, 0));
        panel.add(sizeTextField, GridBagHelper.pos(1, 0));
        panel.add(new JLabel("Sigma: "), GridBagHelper.pos(0, 1));
        panel.add(sigmaTextField, GridBagHelper.pos(1, 1));
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            size = Integer.parseInt(sizeTextField.getText());
            sigma = Double.parseDouble(sigmaTextField.getText());
            updateKernel();
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
