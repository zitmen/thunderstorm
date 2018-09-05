
package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * A very simple helper class for working with java.awt.GridBagLayout class.
 * When adding control elements (labels, buttons, etc.) into a panel
 * ({@code JPanel}) using the {@code GridBagLayout} it is necessary to provide
 * an instance of {@code GridBagLayoutContraints} class for every single control
 * element to specify its position and size in the grid of
 * {@code GridBagLayout}. And this is exactly what this helper provides. So the
 * whole thing simplifies to:
 * 
 * <pre>
 * 
 * {
 * 	&#64;code
 * 	JPanel panel = new JPanel(new GridBagLayout());
 * 	panel.add(new JLabel("Hello!"), GridBagHelper.pos(0, 0));
 * }
 * </pre>
 * 
 * So the whole call of {@code panel.add} can be in a single line without
 * filling the {@code GridBagConstraints} class every single call.
 */
public class GridBagHelper {

	private static GridBagConstraints gbc = new GridBagConstraints();

	private static GridBagConstraints width(int w, int h) {
		gbc.gridwidth = w;
		gbc.gridheight = h;
		return gbc;
	}

	/**
	 * Get a pre-allocated {@code GridBagConstraints} instance filled with X,Y
	 * coordinates of a control element in a grid. <strong>Width and height is
	 * always set to 1!</strong> For changing the width and/or the height use
	 * {@code pos_size} method.
	 *
	 * @param x X coordinate of a control element in a grid
	 * @param y Y coordinate of a control element in a grid
	 * @return the pre-allocated instance of{@code GridBagConstraints} class
	 *         filled with {@code x,y} values specifying the position of a control
	 *         element in a grid.
	 */
	public static GridBagConstraints pos(int x, int y) {
		gbc.gridx = x;
		gbc.gridy = y;
		width(1, 1);
		return gbc;
	}

	/**
	 * Get a pre-allocated {@code GridBagConstraints} instance filled with X,Y
	 * coordinates, width, and height of a control element in a grid.
	 *
	 * @param x X coordinate of a control element in a grid
	 * @param y Y coordinate of a control element in a grid
	 * @param w width of a control element in a grid
	 * @param h height of a control element in a grid
	 * @return the pre-allocated instance of {@code GridBagConstraints} class
	 *         filled with {@code x,y} and {@code w,h} values specifying the
	 *         position, width, and height of a control element in a grid.
	 */
	public static GridBagConstraints pos_size(int x, int y, int w, int h) {
		pos(x, y);
		width(w, h);
		return gbc;
	}

	public static GridBagConstraints leftCol() {
		GridBagConstraints ret = new GridBagConstraints();
		ret.gridx = 0;
		ret.weightx = 0.5;
		ret.anchor = GridBagConstraints.LINE_END;
		ret.insets = new Insets(0, 0, 0, 10);
		return ret;
	}

	public static GridBagConstraints rightCol() {
		GridBagConstraints ret = new GridBagConstraints();
		ret.gridx = 1;
		ret.weightx = 0;
		ret.fill = GridBagConstraints.HORIZONTAL;
		ret.anchor = GridBagConstraints.LINE_START;
		return ret;
	}

	public static GridBagConstraints twoCols() {
		GridBagConstraints ret = new GridBagConstraints();
		ret.gridwidth = 2;
		ret.gridx = 0;
		ret.fill = GridBagConstraints.HORIZONTAL;
		ret.anchor = GridBagConstraints.LINE_START;
		return ret;
	}

	public static class Builder {

		GridBagConstraints gbc;

		public Builder() {
			gbc = new GridBagConstraints();
		}

		public Builder(GridBagConstraints gbc) {
			this.gbc = (GridBagConstraints) gbc.clone();
		}

		public GridBagConstraints build() {
			return gbc;
		}

		public Builder gridx(int x) {
			gbc.gridx = x;
			return this;
		}

		public Builder gridy(int y) {
			gbc.gridy = y;
			return this;
		}

		public Builder gridxy(int x, int y) {
			gbc.gridx = x;
			gbc.gridy = y;
			return this;
		}

		public Builder gridwidth(int gridwidth) {
			gbc.gridwidth = gridwidth;
			return this;
		}

		public Builder gridheight(int gridheight) {
			gbc.gridheight = gridheight;
			return this;
		}

		public Builder weightx(double weightx) {
			gbc.weightx = weightx;
			return this;
		}

		public Builder weighty(double weighty) {
			gbc.weighty = weighty;
			return this;
		}

		public Builder fill(int fill) {
			gbc.fill = fill;
			return this;
		}

		public Builder anchor(int anchor) {
			gbc.anchor = anchor;
			return this;
		}

		public Builder insets(Insets insets) {
			gbc.insets = insets;
			return this;
		}

		public Builder ipadx(int ipadx) {
			gbc.ipadx = ipadx;
			return this;
		}

		public Builder ipady(int ipady) {
			gbc.ipady = ipady;
			return this;
		}
	}
}
