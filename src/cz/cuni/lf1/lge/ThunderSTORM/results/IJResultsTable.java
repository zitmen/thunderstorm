package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;

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
public class IJResultsTable {

  public static final String COLUMN_ID = "#";
  public static final int COLUMN_NOT_FOUND = ResultsTableModel.COLUMN_NOT_FOUND;  // -1
  public static final int COLUMN_IN_USE = -2;
  private static IJResultsTable resultsTable = null;

  /**
   * Returns the ResultsTable used by the Measure command. This table must be
   * displayed in the "Results" window.
   */
  public static IJResultsTable getResultsTable() {
    if (resultsTable == null) {
      setResultsTable(new IJResultsTable());
    }
    return resultsTable;
  }

  public static TripleStateTableModel getModel() {
    return getResultsTable().model;
  }

  public static void setResultsTable(IJResultsTable rt) {
    resultsTable = rt;
  }

  public static boolean isResultsWindow() {
    if (resultsTable == null) {
      return false;
    }
    return resultsTable.tableWindow.isVisible();
  }
  private JavaTableWindow tableWindow;
  private TripleStateTableModel model;

  /**
   * Constructs an empty ResultsTable with the counter=0 and no columns.
   */
  public IJResultsTable() {
    tableWindow = new JavaTableWindow();
    model = tableWindow.getModel();
  }

  /**
   * Displays the contents of this ResultsTable in a window with the default
   * title "ThunderSTORM: Results", or updates an existing results window. Opens
   * a new window if there is no open results window.
   */
  public void show() {
    tableWindow.show();
  }

  /**
   * Displays the contents of this ResultsTable in a window with the specified
   * title, or updates an existing results window. Opens a new window if there
   * is no open results window.
   */
  public void show(String windowTitle) {
    tableWindow.show(windowTitle);
  }

  public void setPreviewRenderer(RenderingQueue renderer) {
    tableWindow.setPreviewRenderer(renderer);
  }

  public synchronized void reset() {
    model.resetAll();
  }
}
