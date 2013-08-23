package cz.cuni.lf1.lge.ThunderSTORM.results;

public class IJPerformanceTable extends GenericTable<GenericTableWindow> {

    public static final String TITLE = "ThunderSTORM: performance evaluation";
    public static final String IDENTIFIER = "performance";
    
    private static IJPerformanceTable performanceTable = null;

    public synchronized static IJPerformanceTable getPerformanceTable() {
        if (performanceTable == null) {
            setPerformanceTable(new IJPerformanceTable());
        }
        return performanceTable;
    }

    public static void setPerformanceTable(IJPerformanceTable pt) {
        performanceTable = pt;
    }

    public static boolean isGroundTruthWindow() {
        if (performanceTable == null) {
            return false;
        }
        return performanceTable.tableWindow.isVisible();
    }
    
    /**
     * Constructs an empty IJPerformanceTable.
     */
    public IJPerformanceTable() {
        super(new PerformanceTableWindow(IJPerformanceTable.TITLE));
    }

    @Override
    public String getFrameTitle() {
        return IJPerformanceTable.TITLE;
    }

    @Override
    public String getTableIdentifier() {
        return IJPerformanceTable.IDENTIFIER;
    }
    
}
