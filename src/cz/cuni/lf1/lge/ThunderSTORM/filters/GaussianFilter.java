package cz.cuni.lf1.lge.ThunderSTORM.filters;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.gauss;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 */
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

    /**
     *
     * @param size
     * @param sigma
     */
    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, Padding.PADDING_DUPLICATE);
        this.size = size;
        this.sigma = sigma;
    }
    
    /**
     *
     * @param size
     * @param sigma
     * @param padding_method
     */
    public GaussianFilter(int size, double sigma, int padding_method) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, padding_method);
        this.size = size;
        this.sigma = sigma;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "Gaussian blur";
    }

    /**
     *
     * @return
     */
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

    /**
     *
     */
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
