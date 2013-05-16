package cz.cuni.lf1.lge.ThunderSTORM.filters;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.mean;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Lowered Gaussian filter is a convolution filter with a kernel calculated as
 * values of a Gaussian function normalized to zero.
 *
 * Such a Gaussian can be calculated as
 * {@code G(sigma,size) - mean(G(sigma,size))}.
 *
 * This filter uses the the same trick with the separable kernels as DoG filter.
 * The only difference here is that one of the filters is the Gaussian filter
 * and the other one is an uniform filter.
 *
 * @see DifferenceOfGaussiansFilter
 * @see ConvolutionFilter
 *
 */
public final class LoweredGaussianFilter implements IFilter {

  private GaussianFilter g;
  private UniformFilter u;
  private int size, padding;
  private double sigma;
  private JTextField sizeTextField, sigmaTextField;

  private void updateKernel() {
    g = new GaussianFilter(size, sigma, padding);
    u = new UniformFilter(size, mean((float[]) g.getKernelX().getPixels()), padding);
  }

  /**
   * Initialize the filter using the Gaussian kernel with specified size 
   * and {@mathjax \sigma} normalized to 0 as described above.
   *
   * @param size size of the kernel
   * @param sigma {@mathjax \sigma} of the Gaussian function
   */
  public LoweredGaussianFilter(int size, double sigma) {
    this.size = size;
    this.sigma = sigma;
    this.padding = Padding.PADDING_DUPLICATE;
    updateKernel();
  }

  /**
   * Initialize the filter using the Gaussian kernel with specified size 
   * and {@mathjax \sigma} normalized to 0 as described above. And also 
   * select one of the padding methods.
   *
   * @param size size of the kernel
   * @param sigma {@mathjax \sigma} of the Gaussian function
   * @param padding_method a padding method
   *
   * @see Padding
   */
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
    size = Integer.parseInt(sizeTextField.getText());
    sigma = Double.parseDouble(sigmaTextField.getText());
    updateKernel();
  }
}
