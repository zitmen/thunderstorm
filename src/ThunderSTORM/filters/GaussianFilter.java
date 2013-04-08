package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.GridBagHelper;
import static ThunderSTORM.utils.Math.gauss;
import ThunderSTORM.utils.Padding;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class GaussianFilter extends ConvolutionFilter implements IModule {
    
    private int size;
    private double sigma;
    
    private JTextField sizeTextField, sigmaTextField;
    
    private static float [] getKernel(int size, double sigma)
    {
        float [] kernel = new float[size];
        for(int i = 0, center = size/2; i < size; i++) {
            kernel[i] = (float) gauss(i - center, sigma, true);
        }
        return kernel;
    }
    
    private void updateKernel() {
        super.updateKernel(new FloatProcessor(1, size, getKernel(size, sigma)), true);
    }

    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, Padding.PADDING_DUPLICATE);
        this.size = size;
        this.sigma = sigma;
    }
    
    public GaussianFilter(int size, double sigma, int padding_method) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, padding_method);
        this.size = size;
        this.sigma = sigma;
    }

    @Override
    public String getName() {
        return "Gaussian blur";
    }

    @Override
    public JPanel getOptionsPanel() {
        sizeTextField = new JTextField(Integer.toString(size), 20);
        sigmaTextField = new JTextField(Double.toString(sigma), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Size: "), GridBagHelper.pos(0,0));
        panel.add(sizeTextField, GridBagHelper.pos(1,0));
        panel.add(new JLabel("Sigma: "), GridBagHelper.pos(0,1));
        panel.add(sigmaTextField, GridBagHelper.pos(1,1));
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
