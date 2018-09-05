
package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.util.LinkedList;
import java.util.Vector;

/**
 * Methods for applying graph algorithms on images.
 */
public class Graph {

	/**
	 * Representation of a connected component. A connected component consists of
	 * nodes, int this particular case those are pixels. All the pixels are stored
	 * in Point structures.
	 * 
	 * @see Point
	 */
	public static class ConnectedComponent {

		private Vector<Point> points = new Vector<Point>();

		/**
		 * Calculate centroid of all points stored in the component. The centroid is
		 * calculated simply as mean value of X,Y position and the intensity of the
		 * centroid is calculated as sum of all the nodes in the component.
		 *
		 * @return a <strong>new instance</strong> of Point class representing the
		 *         calculated centroid
		 */
		public Point centroid() {
			int npts = points.size();
			double[] xarr = new double[npts];
			double[] yarr = new double[npts];
			double[] valarr = new double[npts];

			for (int i = 0, im = npts; i < im; i++) {
				Point p = points.elementAt(i);
				xarr[i] = p.getX().doubleValue();
				yarr[i] = p.getY().doubleValue();
				valarr[i] = p.getVal().doubleValue();
			}
			return new Point(VectorMath.mean(xarr), VectorMath.mean(yarr), VectorMath.sum(valarr));
		}
	}

	/**
	 * South, North, East, and West pixels are connected.
	 * 
	 * <pre>
	 * {@code
	 * .|.
	 * - -
	 * .|.}
	 * </pre>
	 * 
	 * Note that the dots are here used just for sake of formatting.
	 */
	public static final int CONNECTIVITY_4 = 4;
	/**
	 * South, North, East, West, SouthWest, SouthEast, NorthWest, and NorthEast
	 * pixels are connected.
	 * 
	 * <pre>
	 * {@code
	 * \|/
	 * - -
	 * /|\}
	 * </pre>
	 */
	public static final int CONNECTIVITY_8 = 8;

	/**
	 * Get connected components in image. Take an input {@code image} as an
	 * undirected graph where the pixels with value greater than 0 are considered
	 * to be nodes. The edges between them are created accorging to the specified
	 * {@code connectivity} model. Then find <a href=
	 * "http://en.wikipedia.org/wiki/Connected_component_(graph_theory)">connected
	 * components</a> as defined in graph theory.
	 * 
	 * @param ip an input image
	 * @param connectivity one of the connectivity models ({@code CONNECTIVITY_4}
	 *          or {@code CONNECTIVITY_8})
	 * @return Vector of ConnectedComponents
	 * @see ConnectedComponent
	 * @todo This method is much slower than it could be because of too many
	 *       allocations!
	 */
	public static Vector<ConnectedComponent> getConnectedComponents(ij.process.ImageProcessor ip,
		int connectivity)
	{
		assert (ip != null);
		assert ((connectivity == CONNECTIVITY_4) || (connectivity == CONNECTIVITY_8));

		int[][] map = new int[ip.getWidth()][ip.getHeight()];
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				map[x][y] = 0; // member of no component
			}
		}

		Point<Integer> p;
		ConnectedComponent c;
		Vector<ConnectedComponent> components = new Vector<ConnectedComponent>();
		LinkedList<Point<Integer>> queue = new LinkedList<Point<Integer>>();

		int counter = 0;
		boolean n, s, w, e;
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[x].length; y++) {
				if (map[x][y] > 0) continue; // already member of another component
				if (ip.getPixelValue(x, y) == 0.0f) continue; // disabled pixel

				// new component
				counter++;
				queue.clear();
				queue.push(new Point<Integer>(x, y));
				c = new ConnectedComponent();

				while (!queue.isEmpty()) {
					p = queue.pop();
					int px = p.getX().intValue();
					int py = p.getY().intValue();

					if (map[px][py] > 0) continue; // already member of another component
					if (ip.getPixelValue(px, py) == 0.0f) continue; // disabled pixel

					map[px][py] = counter;

					c.points.add(new Point<Float>(p.getX().floatValue(), p.getY().floatValue(), ip
						.getPixelValue(px, py)));

					w = (px > 0); // west
					n = (py > 0); // north
					e = (px < (map.length - 1)); // east
					s = (py < (map[px].length - 1)); // south

					if (w) queue.push(new Point<Integer>(px - 1, py)); // west
					if (n) queue.push(new Point<Integer>(px, py - 1)); // north
					if (e) queue.push(new Point<Integer>(px + 1, py)); // east
					if (s) queue.push(new Point<Integer>(px, py + 1)); // south

					if (connectivity == CONNECTIVITY_8) {
						if (n && w) queue.push(new Point<Integer>(px - 1, py - 1)); // north
																																				// west
						if (n && e) queue.push(new Point<Integer>(px + 1, py - 1)); // north
																																				// east
						if (s && w) queue.push(new Point<Integer>(px - 1, py + 1)); // south
																																				// west
						if (s && e) queue.push(new Point<Integer>(px + 1, py + 1)); // south
																																				// east
					}
				}
				components.add(c);
			}
		}
		return components;
	}
}
