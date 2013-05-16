package cz.cuni.lf1.lge.ThunderSTORM.filters;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.gauss;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Gaussian filter is a convolution filter with its kernel filled with values of normalized
 * 2D Gaussian function written as {@mathjax \frac{1}{\sqrt{2\pi\sigma^2}} e^{-\frac{x^2}{2 \sigma^2}}}.
 *
 * This kernel is symmetric and it is also separable, thus the filter uses the separable kernel feature.
 * 
 * @see ConvolutionFilter
 */
public final class GaussianFilter extends ConvolutionFilter implements IFilter {
    
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
     * Initialize filter to use a kernel with a specified size filled with values
     * of the 2D Gaussian function with a specified {@mathjax \sigma} ({@code sigma}).
     *
     * @param size size of the kernel
     * @param sigma {@mathjax \sigma} of the 2D Gaussian function
     */
    public GaussianFilter(int size, double sigma) {
        super(new FloatProcessor(1, size, getKernel(size, sigma)), true, Padding.PADDING_DUPLICATE);
        this.size = size;
        this.sigma = sigma;
    }
    
    /**
     * Initialize filter to use a kernel with a specified size filled with values
     * of the 2D Gaussian function with a specified {@mathjax \sigma} ({@code sigma})
     * and also set a padding method.
     *
     * @param size size of the kernel
     * @param sigma {@mathjax \sigma} of the 2D Gaussian function
     * @param padding_method a padding method
     * 
     * @see Padding
     */
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
          size = Integer.parseInt(sizeTextField.getText());
          sigma = Double.parseDouble(sigmaTextField.getText());
          updateKernel();
    }
    
}
