package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import java.util.HashMap;
import java.util.List;

public class Thresholder {
    
    private static HashMap<String,ThresholdInterpreter> thresholds = new HashMap<String,ThresholdInterpreter>();
    private static List<IFilterUI> filters = null;
    private static int active_filter = -1;

    public static void setActiveFilter(int index) {
        active_filter = index;
    }
    
    public static void parseThreshold(String formula) throws ThresholdFormulaException {
        thresholds.put(formula, new ThresholdInterpreter(formula));
    }
    
    public static void loadFilters(List<IFilterUI> filters) {
        Thresholder.filters = filters;
    }
    
    public static float getThreshold(String formula) throws ThresholdFormulaException {
        assert(filters != null);
        assert(!filters.isEmpty());
        assert(active_filter >= 0);
        
        if(!thresholds.containsKey(formula)) parseThreshold(formula);
        return thresholds.get(formula).evaluate();
    }

    public static List<IFilterUI> getLoadedFilters() {
        assert(filters != null);
        return filters;
    }
    
    public static IFilter getActiveFilter() {
        assert(filters != null);
        assert(filters.size() > active_filter);
        return filters.get(active_filter).getImplementation();
    }
    
}
