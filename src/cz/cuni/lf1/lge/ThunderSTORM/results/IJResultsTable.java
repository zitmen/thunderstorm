package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import ij.ImagePlus;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Class similar to ImageJ's ResultsTable class containing some of the most
 * frequently used methods.
 *
 * Note that all the deprecated methods were omitted. Also the methods load/save
 * are not present here - use IImportExport instead.
 *
 * Also methods incrementCounter and getCounter are not used since it is
 * useless. In the ImageJ they are used for reallocation of memory, but here ve
 * use collections so wee don't need this functionality.
 *
 * We also do not need to use row labels for anything, hence the related methods
 * are not implemented in this class.
 */
public class IJResultsTable extends GenericTable<ResultsTableWindow> {

    public static final String TITLE = "ThunderSTORM: results";
    public static final String IDENTIFIER = "results";
    private static IJResultsTable resultsTable = null;

    public synchronized static IJResultsTable getResultsTable() {
        if(resultsTable == null) {
            if(SwingUtilities.isEventDispatchThread()) {
                setResultsTable(new IJResultsTable());
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            setResultsTable(new IJResultsTable());
                        }
                    });
                } catch(InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch(InvocationTargetException ex) {
                    throw new RuntimeException(ex.getCause());
                }
            }
        }
        return resultsTable;
    }

    public static void setResultsTable(IJResultsTable rt) {
        resultsTable = rt;
    }

    public static boolean isResultsWindow() {
        if(resultsTable == null) {
            return false;
        }
        return resultsTable.tableWindow.isVisible();
    }
    private ImagePlus analyzedImage;
    private MeasurementProtocol measurementProtocol = null;

    /**
     * Constructs an empty ResultsTable with the counter=0 and no columns.
     */
    public IJResultsTable() {
        super(new ResultsTableWindow(IJResultsTable.TITLE));
    }

    public void setMeasurementProtocol(MeasurementProtocol protocol) {
        measurementProtocol = protocol;
    }

    public MeasurementProtocol getMeasurementProtocol() {
        return measurementProtocol;
    }

    public void setAnalyzedImage(ImagePlus imp) {
        analyzedImage = imp;
    }

    public ImagePlus getAnalyzedImage() {
        return analyzedImage;
    }

    public void repaintAnalyzedImageOverlay() {
        if(analyzedImage != null) {
            analyzedImage.setOverlay(null);
            RenderingOverlay.showPointsInImage(this, analyzedImage, null, Color.red, RenderingOverlay.MARKER_CROSS);
        }
    }

    @Override
    public void reset() {
        super.reset();
        tableWindow.setPreviewRenderer(null);
        tableWindow.getOperationHistoryPanel().removeAllOperations();
        tableWindow.setStatus(null);
    }

    //delegated methods from window
    public void showPreview() {
        tableWindow.showPreview();
    }

    public void setLivePreview(boolean enabled) {
        tableWindow.setLivePreview(enabled);
    }

    public OperationsHistoryPanel getOperationHistoryPanel() {
        return tableWindow.getOperationHistoryPanel();
    }

    public void setPreviewRenderer(RenderingQueue renderer) {
        tableWindow.setPreviewRenderer(renderer);
    }

    public void setStatus(String text) {
        tableWindow.setStatus(text);
    }

    public List<? extends PostProcessingModule> getPostProcessingModules() {
        return tableWindow.getPostProcessingModules();
    }

    void addNewFilter(String paramName, double greaterThan, double lessThan) {
        ResultsFilter filter = tableWindow.getFilter();
        String formula = filter.getFilterFormula().trim();
        StringBuilder sb = new StringBuilder(formula);
        if(!formula.isEmpty()) {
            sb.append(" & ");
        }
        sb.append("(");
        sb.append(paramName).append(" > ").append(BigDecimal.valueOf(greaterThan).round(new MathContext(6)).toString());
        sb.append(" & ");
        sb.append(paramName).append(" < ").append(BigDecimal.valueOf(lessThan).round(new MathContext(6)).toString());
        sb.append(")");
        filter.setFilterFormula(sb.toString());
    }

    @Override
    public String getFrameTitle() {
        return IJResultsTable.TITLE;
    }

    @Override
    public String getTableIdentifier() {
        return IJResultsTable.IDENTIFIER;
    }
}
    