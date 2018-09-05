
package cz.cuni.lf1.lge.ThunderSTORM.filters;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.thunderstorm.algorithms.filters.MedianFilterPattern;
import ij.process.FloatProcessor;

/**
 * Apply a median filter on an input image.
 */
public final class MedianFilter implements IFilter {

	private FloatProcessor input = null, result = null;
	private HashMap<String, FloatProcessor> export_variables = null;
	/**
	 * Setting the cross pattern will calculate the median of 5 values (center,
	 * left, right, top, and bottom). Cross pattern:
	 * 
	 * <pre>
	 * {@code
	 * .#.
	 * ###
	 * .#.}
	 * </pre>
	 */
	public static final int CROSS = 4;
	/**
	 * Setting the cross pattern will calculate the median of all 9 values. Box
	 * pattern:
	 * 
	 * <pre>
	 * {@code
	 * ###
	 * ###
	 * ###}
	 * </pre>
	 */
	public static final int BOX = 8;
	private cz.cuni.lf1.thunderstorm.algorithms.filters.MedianFilter filter;

	public MedianFilter() {
		this(BOX, 3);
	}

	/**
	 * Initialize the filter.
	 *
	 * @param pattern one of the pre-defined patterns ({@code BOX or CROSS})
	 * @param size size of the median filter, typically 3, 5, or 7, which selects
	 *          points from a box of size 3x3, 5x5, or 7x7 respectively
	 */
	public MedianFilter(int pattern, int size) {
		assert ((pattern == BOX) || (pattern == CROSS));

		filter = new cz.cuni.lf1.thunderstorm.algorithms.filters.MedianFilter(pattern == BOX
			? MedianFilterPattern.BOX : MedianFilterPattern.CROSS, size);
	}

	/**
	 * Go through the input {@code image}, calculate median at each position, and
	 * save the result into the output image at the same position as the median
	 * was calculated at.
	 *
	 * @param image an input image
	 * @return a <strong>new instance</strong> of FloatProcessor that contains the
	 *         filtered image (matrix of medians)
	 */
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
		return "Med";
	}

	@NotNull
	@Override
	public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
		if (export_variables == null) {
			export_variables = new HashMap<String, FloatProcessor>();
		}
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
