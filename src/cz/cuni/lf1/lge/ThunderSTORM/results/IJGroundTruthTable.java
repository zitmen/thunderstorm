package cz.cuni.lf1.lge.ThunderSTORM.results;

public class IJGroundTruthTable extends GenericTable<GroundTruthTableWindow> {

    public static final String TITLE = "ThunderSTORM: ground-truth";
    public static final String IDENTIFIER = "ground-truth";
    
    private static IJGroundTruthTable gtTable = null;

    /**
     * Returns the ResultsTable used by the Measure command. This table must be
     * displayed in the "Results" window.
     */
    public synchronized static IJGroundTruthTable getGroundTruthTable() {
        if (gtTable == null) {
            setGroundTruthTable(new IJGroundTruthTable());
        }
        return gtTable;
    }

    public static void setGroundTruthTable(IJGroundTruthTable gt) {
        gtTable = gt;
    }

    public static boolean isGroundTruthWindow() {
        if (gtTable == null) {
            return false;
        }
        return gtTable.tableWindow.isVisible();
    }
    
    /**
     * Constructs an empty GroundTruthTable.
     */
    public IJGroundTruthTable() {
        super(new GroundTruthTableWindow(IJGroundTruthTable.TITLE));
    }

    @Override
    public String getFrameTitle() {
        return IJGroundTruthTable.TITLE;
    }

    @Override
    public String getTableIdentifier() {
        return IJGroundTruthTable.IDENTIFIER;
    }

}
