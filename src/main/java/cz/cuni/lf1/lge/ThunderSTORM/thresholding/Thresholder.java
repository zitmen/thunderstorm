
package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.process.FloatProcessor;

public class Thresholder {

	static private List<IFilterUI> filters = null;
	static private int active_filter = -1;

	private final static class Data {

		public HashMap<String, ThresholdInterpreter> thresholds;
		public List<IFilter> filters;
		public int active_filter;
		public FloatProcessor image;

		public Data() {
			this.thresholds = new HashMap<String, ThresholdInterpreter>();
			this.filters = null;
			this.active_filter = -1;
			this.image = null;
		}
	}

	private final static class ThreadLocalData extends ThreadLocal<Data> {

		@Override
		protected synchronized Data initialValue() {
			Data data = new Data();
			if (Thresholder.filters != null) {
				// Filters must to be duplicated for each thread,
				// if they are set before the analysis starts!
				// This is the same for all the threads -> this function
				// is just for distribution of the information between all
				// the worker threads.
				data.filters = new ArrayList<IFilter>();
				for (IFilterUI filter : Thresholder.filters) {
					data.filters.add(filter.getThreadLocalImplementation());
				}
				data.active_filter = Thresholder.active_filter;
			}
			return data;
		}
	}

	private static ThreadLocal<Data> data = new ThreadLocalData();

	public static synchronized void loadFilters(List<IFilterUI> filters) {
		Thresholder.filters = filters;
		data = new ThreadLocalData();
	}

	public static synchronized void setActiveFilter(int index) {
		Thresholder.active_filter = index;
		data.get().active_filter = index;
	}

	public static void parseThreshold(String formula) throws FormulaParserException {
		data.get().thresholds.put(formula, new ThresholdInterpreter(formula));
	}

	public static float getThreshold(String formula) throws FormulaParserException {
		// assert(filters != null);
		// assert(!filters.isEmpty());
		// assert(active_filter >= 0);

		if (!data.get().thresholds.containsKey(formula)) {
			parseThreshold(formula);
		}
		return data.get().thresholds.get(formula).evaluate();
	}

	public static List<IFilter> getLoadedFilters() {
		assert (data.get().filters != null);

		return data.get().filters;
	}

	public static IFilter getActiveFilter() {
		assert (data.get().filters != null);
		assert (data.get().filters.size() > data.get().active_filter);

		return data.get().filters.get(data.get().active_filter);
	}

	public static void setCurrentImage(FloatProcessor fp) {
		data.get().image = fp;
	}

	public static FloatProcessor getCurrentImage() {
		assert (data.get().image != null);

		return data.get().image;
	}
}
