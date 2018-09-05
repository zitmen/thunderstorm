
package cz.cuni.lf1.lge.ThunderSTORM.filters;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding;
import ij.process.FloatProcessor;

/**
 * Box filter is a uniform convolution filter with its kernel filled with ones,
 * i.e., it is a mean filter, because it calculates mean value of intensities of
 * surrounding pixels. This filter uses the separable kernel feature.
 */
public final class BoxFilter implements IFilter {

	private FloatProcessor input;
	private FloatProcessor result;
	private cz.cuni.lf1.thunderstorm.algorithms.filters.BoxFilter mFilter;

	/**
	 * Initialize the filter.
	 *
	 * @param size size of a box (if size is 5, then the box is 5x5 pixels)
	 */
	public BoxFilter(int size) {
		mFilter = new cz.cuni.lf1.thunderstorm.algorithms.filters.BoxFilter(size,
			DuplicatePadding::new);
	}

	@Override
	public String getFilterVarName() {
		return "Box";
	}

	@NotNull
	@Override
	public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
		HashMap<String, FloatProcessor> export_variables = new HashMap<>();

		if (reevaluate) {
			filterImage(Thresholder.getCurrentImage());
		}
		//
		export_variables.put("I", input);
		export_variables.put("F", result);
		return export_variables;
	}

	@NotNull
	@Override
	public FloatProcessor filterImage(@NotNull FloatProcessor image) {
		GUI.checkIJEscapePressed();

		input = image;
		result = GrayScaleImageImpl.convertToFloatProcessor(mFilter.filter(new GrayScaleImageImpl(
			image)));
		return result;
	}
}
