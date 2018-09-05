
package cz.cuni.lf1.lge.ThunderSTORM.filters;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter;
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding;
import ij.process.FloatProcessor;

/**
 * Filtering by Difference of Gaussians (DoG) is a convolution with a kernel
 * calculated as subtraction of two different Gaussian kernels. DoG can be an
 * approximation of Laplacian of Gaussian (LoG) also called Mexican Hat. This
 * kernel is not separable, but it is still possible to use some tricks to speed
 * things up. We have implemented this filter as follows:
 * <ol>
 * <li>convolve the input image with Gaussian filter no. 1</li>
 * <li>convolve the input image with Gaussian filter no. 2</li>
 * <li>subtract the two convolved images to get a DoG filtered image</li>
 * </ol>
 * Let us recall that convolution with a full (matrix) kernel takes
 * {@mathjax (W_i \cdot H_i) \cdot (W_k \cdot H_k)} iterations and that
 * convolution with a separable kernel takes {@mathjax (W_i \cdot H_i) \cdot
 * (W_k + H_k)} iterations. The implemented algorithm uses two convolutions with
 * separable kernels and one image subtraction, i.e., {@mathjax (W_i \cdot H_i)
 * \cdot (2W_k + 2H_k + 1)} iterations, which is asymptotically faster than
 * convolution with the full kernel. It is quite obvious that the current
 * implementation should be faster for big kernels. However a question raises
 * for small kernels: how much is the performance affected due to the allocation
 * of memory for two images instead of one and due to the subtraction of these
 * images. Since the smallest reasonable kernel size is 3x3, this is not
 * supposed to be an issue.
 */
public final class DifferenceOfGaussiansFilter implements IFilter {

	private FloatProcessor input = null, result = null, result_g1 = null, result_g2 = null;
	private HashMap<String, FloatProcessor> export_variables = null;

	private cz.cuni.lf1.thunderstorm.algorithms.filters.DifferenceOfGaussiansFilter dog;
	private cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter g1, g2;

	/**
	 * Initialize the filter with a kernel of specified size and
	 * {@mathjax \sigma_1} for first Gaussian and {@mathjax \sigma_2} for the
	 * second one.
	 * <p>
	 * The second Gaussian is subtracted from the first one, i.e., {@mathjax K =
	 * G(size,\sigma_1) - G(size,\sigma_2)}, where {@mathjax K} is a kernel matrix
	 * and {@mathjax G} stands for the Gaussian function given by {@mathjax size}
	 * and a particular value of {@mathjax \sigma}.
	 *
	 * @param size size of the kernel
	 * @param sigma_g1 {@mathjax \sigma} of the first Gaussian
	 * @param sigma_g2 {@mathjax \sigma} of the second Gaussian
	 */
	public DifferenceOfGaussiansFilter(int size, double sigma_g1, double sigma_g2) {
		g1 = new GaussianFilter(size, sigma_g1, DuplicatePadding::new);
		g2 = new GaussianFilter(size, sigma_g2, DuplicatePadding::new);
		dog = new cz.cuni.lf1.thunderstorm.algorithms.filters.DifferenceOfGaussiansFilter(size,
			sigma_g1, sigma_g2, DuplicatePadding::new);
	}

	@NotNull
	@Override
	public FloatProcessor filterImage(@NotNull FloatProcessor image) {
		GUI.checkIJEscapePressed();

		input = image;
		result_g1 = GrayScaleImageImpl.convertToFloatProcessor(g1.filter(new GrayScaleImageImpl(
			image)));
		result_g2 = GrayScaleImageImpl.convertToFloatProcessor(g2.filter(new GrayScaleImageImpl(
			image)));
		result = GrayScaleImageImpl.convertToFloatProcessor(dog.filter(new GrayScaleImageImpl(image)));
		return result;
	}

	@Override
	public String getFilterVarName() {
		return "DoG";
	}

	@NotNull
	@Override
	public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
		if (export_variables == null) export_variables = new HashMap<>();
		//
		if (reevaluate) {
			filterImage(Thresholder.getCurrentImage());
		}
		//
		export_variables.put("I", input);
		export_variables.put("F", result);
		export_variables.put("G1", result_g1);
		export_variables.put("G2", result_g2);
		return export_variables;
	}
}
