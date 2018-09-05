
package cz.cuni.lf1.lge.ThunderSTORM.util;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.pow;

import java.util.Arrays;

import ij.process.FloatProcessor;

/**
 * Helper class to offer some additional functionality over the ImageProcessor
 * from ImageJ.
 */
public class ImageMath {

	public static FloatProcessor ones(int width, int height) {
		float[] matrix = new float[height * width];
		Arrays.fill(matrix, 1f);
		return new FloatProcessor(width, height, matrix, null);
	}

	/**
	 * Add two images. The images are required to be of the same size.
	 *
	 * @param fp1 an input image which the other input image ({@code fp2}) will be
	 *          added to
	 * @param fp2 another input image which will be added to {@code fp1}
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fp3 =
	 *         fp1 + fp2}
	 */
	public static FloatProcessor add(FloatProcessor fp1, FloatProcessor fp2) {
		assert (fp1.getWidth() == fp2.getWidth());
		assert (fp1.getHeight() == fp2.getHeight());

		FloatProcessor out = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
		out.setMask(fp1.getMask() != null ? fp1.getMask() : fp2.getMask());
		for (int i = 0, im = fp1.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp1.getHeight(); j < jm; j++) {
				out.setf(i, j, fp1.getPixelValue(i, j) + fp2.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Add a scalar value to an image.
	 * 
	 * @param val an input value will be added to the input image ({@code fp})
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         val + fp}
	 */
	public static FloatProcessor add(float val, FloatProcessor fp) {
		FloatProcessor out = new FloatProcessor(fp.getWidth(), fp.getHeight());
		out.setMask(fp.getMask());
		for (int i = 0, im = fp.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp.getHeight(); j < jm; j++) {
				out.setf(i, j, val + fp.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Subtract one image to the other. The images are required to be of the same
	 * size.
	 *
	 * @param fp1 an input image which the other input image ({@code fp2}) will be
	 *          subtracted from
	 * @param fp2 another input image which will be subtracted from {@code fp1}
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fp3 =
	 *         fp1 - fp2}
	 */
	public static FloatProcessor subtract(FloatProcessor fp1, FloatProcessor fp2) {
		assert (fp1.getWidth() == fp2.getWidth());
		assert (fp1.getHeight() == fp2.getHeight());

		FloatProcessor out = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
		out.setMask(fp1.getMask() != null ? fp1.getMask() : fp2.getMask());
		for (int i = 0, im = fp1.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp1.getHeight(); j < jm; j++) {
				out.setf(i, j, fp1.getPixelValue(i, j) - fp2.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Subtract an image from a scalar.
	 * 
	 * @param val an input value which the input image ({@code fp}) will be
	 *          subtracted from
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         val - fp}
	 */
	public static FloatProcessor subtract(float val, FloatProcessor fp) {
		FloatProcessor out = new FloatProcessor(fp.getWidth(), fp.getHeight());
		out.setMask(fp.getMask());
		for (int i = 0, im = fp.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp.getHeight(); j < jm; j++) {
				out.setf(i, j, val - fp.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Subtract a scalar value from an image.
	 * 
	 * @param val an input value which will be substracted from the input image
	 *          ({@code fp})
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         fp - val}
	 */
	public static FloatProcessor subtract(FloatProcessor fp, float val) {
		return add(-val, fp);
	}

	/**
	 * Multiply two images. The images are required to be of the same size.
	 *
	 * @param fp1 an input image which the other input image ({@code fp2}) will be
	 *          multiplied with
	 * @param fp2 another input image which will be multiplied with {@code fp1}
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fp3 =
	 *         fp1 * fp2}
	 */
	public static FloatProcessor multiply(FloatProcessor fp1, FloatProcessor fp2) {
		assert (fp1.getWidth() == fp2.getWidth());
		assert (fp1.getHeight() == fp2.getHeight());

		FloatProcessor out = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
		out.setMask(fp1.getMask() != null ? fp1.getMask() : fp2.getMask());
		for (int i = 0, im = fp1.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp1.getHeight(); j < jm; j++) {
				out.setf(i, j, fp1.getPixelValue(i, j) * fp2.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Multiply an image by a scalar value.
	 * 
	 * @param val an input value which the input image ({@code fp}) will be
	 *          multiplied with
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         val * fp}
	 */
	public static FloatProcessor multiply(float val, FloatProcessor fp) {
		FloatProcessor out = new FloatProcessor(fp.getWidth(), fp.getHeight());
		out.setMask(fp.getMask());
		for (int i = 0, im = fp.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp.getHeight(); j < jm; j++) {
				out.setf(i, j, val * fp.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Divide values of one image by values of the other image. The images are
	 * required to be of the same size.
	 *
	 * @param fp1 an input image which the other input image ({@code fp2}) will be
	 *          divided by
	 * @param fp2 another input image which will divide the {@code fp1}
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fp3 =
	 *         fp1 / fp2}
	 */
	public static FloatProcessor divide(FloatProcessor fp1, FloatProcessor fp2) {
		assert (fp1.getWidth() == fp2.getWidth());
		assert (fp1.getHeight() == fp2.getHeight());

		FloatProcessor out = new FloatProcessor(fp1.getWidth(), fp1.getHeight());
		out.setMask(fp1.getMask() != null ? fp1.getMask() : fp2.getMask());
		for (int i = 0, im = fp1.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp1.getHeight(); j < jm; j++) {
				out.setf(i, j, fp1.getPixelValue(i, j) / fp2.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Divide a scalar value by values from an image.
	 * 
	 * @param val an input value which the input image ({@code fp}) will divide
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         val / fp}
	 */
	public static FloatProcessor divide(float val, FloatProcessor fp) {
		FloatProcessor out = new FloatProcessor(fp.getWidth(), fp.getHeight());
		out.setMask(fp.getMask());
		for (int i = 0, im = fp.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp.getHeight(); j < jm; j++) {
				out.setf(i, j, val / fp.getPixelValue(i, j));
			}
		}

		return out;
	}

	/**
	 * Divide an image by a scalar value.
	 * 
	 * @param val an input value which will divide the input image ({@code fp})
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         fp / val}
	 */
	public static FloatProcessor divide(FloatProcessor fp, float val) {
		return multiply(1.0f / val, fp);
	}

	/**
	 * Calculate a {@code val}-th power of an image {@code fp}.
	 * 
	 * @param val {@code val}-th power of {@code fp}
	 * @param fp an input image
	 * @return a <strong>new instance</strong> of FloatProcessor: {@mathjax fpv =
	 *         fp ^ val}
	 */
	public static FloatProcessor power(FloatProcessor fp, float val) {
		FloatProcessor out = new FloatProcessor(fp.getWidth(), fp.getHeight());
		out.setMask(fp.getMask());
		for (int i = 0, im = fp.getWidth(); i < im; i++) {
			for (int j = 0, jm = fp.getHeight(); j < jm; j++) {
				out.setf(i, j, (float) pow((double) fp.getPixelValue(i, j), (double) val));
			}
		}

		return out;
	}

	/**
	 * Apply a binary {@code threshold} to the {@code image}. Instead of
	 * implicitly setting the pixels in thresholded image to 0 and 1, the pixels
	 * with their values equal or greater than threshold are set to
	 * {@code high_val}. The rest is res to {@code low_value}. Note that this
	 * method <strong>modifies</strong> the input image.
	 * 
	 * @param image an input image
	 * @param threshold a threshold value
	 * @param low_val value that the pixels with values lesser then
	 *          {@code threshold} are set to
	 * @param high_val value that the pixels with values equal or greater then
	 *          {@code threshold} are set to
	 */
	public static void threshold(FloatProcessor image, float threshold, float low_val,
		float high_val)
	{
		for (int i = 0, im = image.getWidth(); i < im; i++) {
			for (int j = 0, jm = image.getHeight(); j < jm; j++) {
				if (image.getPixelValue(i, j) >= threshold) {
					image.setf(i, j, high_val);
				}
				else {
					image.setf(i, j, low_val);
				}
			}
		}
	}

	/**
	 * Apply a {@code mask} to an input {@code image}. The masking works simply by
	 * checking where is a 0 in the mask image and setting the corresponding pixel
	 * in the input image to 0 as well. Hence the mask does not have to be binary.
	 * For example let's have the following input image:
	 * 
	 * <pre>
	 * {@code
	 * 123
	 * 456
	 * 789}
	 * </pre>
	 * 
	 * and the following mask:
	 * 
	 * <pre>
	 * {@code
	 * 560
	 * 804
	 * 032}
	 * </pre>
	 * 
	 * Then the result of applying the mask is:
	 * 
	 * <pre>
	 * {@code
	 * 120
	 * 406
	 * 089}
	 * </pre>
	 * 
	 * @param image an input image
	 * @param mask a mask image
	 * @return a <strong>new instance</strong> of FloatProcessor that contains the
	 *         input image after the mask was applied
	 */
	public static FloatProcessor applyMask(FloatProcessor image, FloatProcessor mask) {
		assert (image.getWidth() == mask.getWidth());
		assert (image.getHeight() == mask.getHeight());

		FloatProcessor result = new FloatProcessor(image.getWidth(), image.getHeight(), (float[]) image
			.getPixelsCopy(), null);
		for (int x = 0, xm = image.getWidth(); x < xm; x++) {
			for (int y = 0, ym = image.getHeight(); y < ym; y++) {
				if (mask.getf(x, y) == 0.0f) {
					result.setf(x, y, 0.0f);
				}
			}
		}

		return result;
	}

	public static FloatProcessor relEq(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a<b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, ((a.getf(x, y) == b.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relNeq(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a<b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, ((a.getf(x, y) != b.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relGt(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a<b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, ((a.getf(x, y) > b.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relLt(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a<b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, ((a.getf(x, y) < b.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relLt(Double val, FloatProcessor mat) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((v < mat.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relLt(FloatProcessor mat, Double val) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((mat.getf(x, y) < v) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relGt(Double val, FloatProcessor mat) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((v > mat.getf(x, y)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relGt(FloatProcessor mat, Double val) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((mat.getf(x, y) > v) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor logAnd(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a&b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, (((a.getf(x, y) != 0.0f) && (b.getf(x, y) != 0.0f)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor logOr(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a|b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		for (int x = 0; x < a.getWidth(); x++) {
			for (int y = 0; y < a.getHeight(); y++) {
				res.setf(x, y, (((a.getf(x, y) != 0.0f) || (b.getf(x, y) != 0.0f)) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relEq(Double val, FloatProcessor mat) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((mat.getf(x, y) == v) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relEq(FloatProcessor mat, Double val) {
		return relEq(val, mat);
	}

	public static FloatProcessor relNeq(Double val, FloatProcessor mat) {
		float v = val.floatValue();
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		for (int x = 0; x < mat.getWidth(); x++) {
			for (int y = 0; y < mat.getHeight(); y++) {
				res.setf(x, y, ((mat.getf(x, y) != v) ? 1.0f : 0.0f));
			}
		}
		return res;
	}

	public static FloatProcessor relNeq(FloatProcessor mat, Double val) {
		return relNeq(val, mat);
	}

	public static FloatProcessor abs(FloatProcessor mat) {
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight(), (float[]) mat
			.getPixelsCopy(), null);
		res.abs();
		res.setMask(mat.getMask());
		return res;
	}

	public static FloatProcessor modulo(float val, FloatProcessor mat) {
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		float tmp;
		for (int i = 0, im = mat.getWidth(); i < im; i++) {
			for (int j = 0, jm = mat.getHeight(); j < jm; j++) {
				tmp = val / mat.getf(i, j);
				res.setf(i, j, val - (((float) ((int) tmp)) * mat.getf(i, j)));
			}
		}
		return res;
	}

	public static FloatProcessor modulo(FloatProcessor mat, float val) {
		FloatProcessor res = new FloatProcessor(mat.getWidth(), mat.getHeight());
		res.setMask(mat.getMask());
		float tmp;
		for (int i = 0, im = mat.getWidth(); i < im; i++) {
			for (int j = 0, jm = mat.getHeight(); j < jm; j++) {
				tmp = mat.getf(i, j) / val;
				res.setf(i, j, mat.getf(i, j) - (((float) ((int) tmp)) * val));
			}
		}
		return res;
	}

	public static FloatProcessor modulo(FloatProcessor a, FloatProcessor b) {
		if ((a.getWidth() != b.getWidth()) || (a.getHeight() != b.getHeight())) {
			throw new IllegalArgumentException(
				"Error during evaluation of `a%b` expression! Both operands must be of the same size!");
		}
		FloatProcessor res = new FloatProcessor(a.getWidth(), a.getHeight());
		res.setMask(a.getMask() != null ? a.getMask() : b.getMask());
		float tmp;
		for (int i = 0, im = a.getWidth(); i < im; i++) {
			for (int j = 0, jm = a.getHeight(); j < jm; j++) {
				tmp = a.getf(i, j) / b.getf(i, j);
				res.setf(i, j, a.getf(i, j) - (((float) ((int) tmp)) * b.getf(i, j)));
			}
		}
		return res;
	}

}
