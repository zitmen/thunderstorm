
package cz.cuni.lf1.lge.ThunderSTORM.filters;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding;
import ij.process.FloatProcessor;

/**
 * Lowered Gaussian filter is a convolution filter with a kernel calculated as
 * values of a Gaussian function normalized to zero. Such a Gaussian can be
 * calculated as {@code G(sigma,size) - mean(G(sigma,size))}. This filter uses
 * the the same trick with the separable kernels as DoG filter. The only
 * difference here is that one of the filters is the Gaussian filter and the
 * other one is an uniform filter.
 *
 * @see DifferenceOfGaussiansFilter
 */
public final class LoweredGaussianFilter implements IFilter {

	private FloatProcessor input = null, result = null;
	private HashMap<String, FloatProcessor> export_variables;

	private cz.cuni.lf1.thunderstorm.algorithms.filters.LoweredGaussianFilter filter;

	private int size;
	private Padding padding;
	private double sigma;

	public LoweredGaussianFilter() {
		this(11, 1.6);
	}

	/**
	 * Initialize the filter using the Gaussian kernel with specified size and
	 * {@mathjax \sigma} normalized to 0 as described above.
	 *
	 * @param size size of the kernel
	 * @param sigma {@mathjax \sigma} of the Gaussian function
	 */
	public LoweredGaussianFilter(int size, double sigma) {
		filter = new cz.cuni.lf1.thunderstorm.algorithms.filters.LoweredGaussianFilter(size, sigma,
			DuplicatePadding::new);
	}

	@NotNull
	@Override
	public FloatProcessor filterImage(@NotNull FloatProcessor image) {
		input = image;
		result = GrayScaleImageImpl.convertToFloatProcessor(filter.filter(new GrayScaleImageImpl(
			image)));
		return result;
	}

	@Override
	public String getFilterVarName() {
		return "LowGauss";
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
		return export_variables;
	}
}
