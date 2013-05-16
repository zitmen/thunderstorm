package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filtering by Difference of Gaussians (DoG) is a convolution with a kernel
 * calculated as subtraction of two different Gaussian kernels.
 * 
 * DoG can be an approximation of Laplacian of Gaussian (LoG) also called Mexican Hat.
 * This kernel is not separable, but it is still possible to use some tricks to speed things up.
 * We have implemented this filter as follows:
 * <ol>
 *     <li>convolve the input image with Gaussian filter no. 1</li>
 *     <li>convolve the input image with Gaussian filter no. 2</li>
 *     <li>subtract the two convolved images to get a DoG filtered image</li>
 * </ol>
 * 
 * Let us recall that convolution with a full (matrix) kernel takes {@mathjax (W_i \cdot H_i) \cdot (W_k \cdot H_k)}
 * iterations and that convolution with a separable kernel takes {@mathjax (W_i \cdot H_i) \cdot (W_k + H_k)}
 * iterations. The implemented algorithm uses two convolutions with separable kernels and one image subtraction,
 * i.e., {@mathjax (W_i \cdot H_i) \cdot (2W_k + 2H_k + 1)} iterations, which is asymptotically faster than
 * convolution with the full kernel.
 * 
 * It is quite obvious that the current implementation should be faster for big kernels. However a question raises
 * for small kernels: how much is the performance affected due to the allocation of memory for two images instead of one
 * and due to the subtraction of these images.
 * Since the smallest reasonable kernel size is 3x3, this is not supposed to be an issue.
 * 
 * @see ConvolutionFilter
 */
public final class DifferenceOfGaussiansFilter implements IFilter, IModule {

    private int size, padding;
    private double sigma_g1, sigma_g2;
    
    private JTextField sigma1TextField, sigma2TextField, sizeTextField;
    
    private GaussianFilter g1, g2;
    
    private void updateKernels() {
        g1 = new GaussianFilter(size, sigma_g1, padding);
        g2 = new GaussianFilter(size, sigma_g2, padding);
    }
    
    /**
     * Initialize the filter with a kernel of specified size and {@mathjax \sigma_1}
     * for first Gaussian and {@mathjax \sigma_2} for the second one.
     * 
     * The second Gaussian is subtracted from the first one, i.e.,
     * {@mathjax K = G(size,\sigma_1) - G(size,\sigma_2)}, where {@mathjax K} is a kernel matrix
     * and {@mathjax G} stands for the Gaussian function given by {@mathjax size} and a particular
     * value of {@mathjax \sigma}.
     *
     * @param size size of the kernel
     * @param sigma_g1 {@mathjax \sigma} of the first Gaussian
     * @param sigma_g2 {@mathjax \sigma} of the second Gaussian
     */
    public DifferenceOfGaussiansFilter(int size, double sigma_g1, double sigma_g2) {
        this.size = size;
        this.sigma_g1 = sigma_g1;
        this.sigma_g2 = sigma_g2;
        this.padding = Padding.PADDING_DUPLICATE;
        updateKernels();
    }
    
    /**
     * Initialize the filter with a kernel of specified size and {@mathjax \sigma_1}
     * for first Gaussian and {@mathjax \sigma_2} for the second one.
     * 
     * The second Gaussian is subtracted from the first one, i.e.,
     * {@mathjax K = G(size,\sigma_1) - G(size,\sigma_2)}, where {@mathjax K} is a kernel matrix
     * and {@mathjax G} stands for the Gaussian function given by {@mathjax size} and a particular
     * value of {@mathjax \sigma}.
     *
     * @param size size of the kernel
     * @param sigma_g1 {@mathjax \sigma} of the first Gaussian
     * @param sigma_g2 {@mathjax \sigma} of the second Gaussian
     * @param padding_method a padding method
     * 
     * @see Padding
     */
    public DifferenceOfGaussiansFilter(int size, double sigma_g1, double sigma_g2, int padding_method) {
        this.size = size;
        this.sigma_g1 = sigma_g1;
        this.sigma_g2 = sigma_g2;
        this.padding = padding_method;
        updateKernels();
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return ImageProcessor.subtractImage(g1.filterImage(image), g2.filterImage(image));
    }

    @Override
    public String getName() {
        return "Difference of Gaussians";
    }

    @Override
    public JPanel getOptionsPanel() {
        sizeTextField = new JTextField(Integer.toString(size), 20);
        sigma1TextField = new JTextField(Double.toString(sigma_g1), 20);
        sigma2TextField = new JTextField(Double.toString(sigma_g2), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Size: "), GridBagHelper.pos(0,0));
        panel.add(sizeTextField, GridBagHelper.pos(1,0));
        panel.add(new JLabel("Sigma1: "), GridBagHelper.pos(0,1));
        panel.add(sigma1TextField, GridBagHelper.pos(1,1));
        panel.add(new JLabel("Sigma2: "), GridBagHelper.pos(0,2));
        panel.add(sigma2TextField, GridBagHelper.pos(1,2));
        return panel;
    }

    @Override
    public void readParameters() {
        size = Integer.parseInt(sizeTextField.getText());
        sigma_g1 = Double.parseDouble(sigma1TextField.getText());
        sigma_g2 = Double.parseDouble(sigma2TextField.getText());
        updateKernels();
    }
    
}
