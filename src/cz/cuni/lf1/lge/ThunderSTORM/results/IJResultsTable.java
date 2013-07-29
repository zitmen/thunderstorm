package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import java.util.Vector;

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

  /**
   * Increments the measurement counter by one.
   */
  public synchronized void addRow() {
    model.addRow();
  }


  /**
   * Returns true if the specified column exists and is not empty.
   */
  public boolean columnExists(int column) {
    return ((column >= 0) && (column < model.getColumnCount()));
  }

  public boolean columnExists(String column) {
    return (model.findColumn(column) != ResultsTableModel.COLUMN_NOT_FOUND);
  }

  /**
   * Returns the index of the first column with the given heading. heading. If
   * not found, returns COLUMN_NOT_FOUND.
   */
  public int getColumnIndex(String heading) {
    return model.findColumn(heading);
  }

  /**
   * Returns the Double value of the given column and row, where column must be
   * less than or equal the value returned by getLastColumn() and row must be
   * greater than or equal zero and less than the value returned by
   * getRowCount().
   */
  public Double getValueAsDoubleObject(int column, int row) {
    return (Double) model.getValueAt(row, column);
  }

  /**
   * Returns the double value of the given column and row, where column must be
   * less than or equal the value returned by getLastColumn() and row must be
   * greater than or equal zero and less than the value returned by
   * getRowCount().
   */
  public double getValueAsDouble(int column, int row) {
    return getValueAsDoubleObject(column, row).doubleValue();
  }

  /**
   * Returns the value of the specified column and row, where column is the
   * column heading and row is a number greater than or equal zero and less than
   * value returned by getRowCount(). Throws an IllegalArgumentException if this
   * ResultsTable does not have a column with the specified heading.
   */
  public double getValue(String column, int row) {
    if (!columnExists(column)) {
      throw new IllegalArgumentException("Column `" + column + "` does not exist!");
    }
    return getValueAsDouble(row, model.findColumn(column));
  }

  public int getRowCount() {
    return model.getRowCount();
  }

  public int getColumnCount() {
    return model.getColumnCount();
  }


  /**
   * Returns the heading of the specified column or null if the column is empty.
   */
  public String getColumnHeading(int column) {
    if ((column >= 0) && (column < model.getColumnCount())) {
      return model.getColumnName(column);
    }
    return null;
  }

  /**
   * Returns a tab or comma delimited string containing the column headings.
   */
  public String getColumnHeadings() {
    StringBuilder builder = new StringBuilder();
    for (String heading : model.getColumnNames()) {
      builder.append(heading);
      builder.append(',');
    }
    builder.deleteCharAt(builder.length() - 1);
    return builder.toString();
  }

  public synchronized void reset() {
    model.resetAll();
  }

  /**
   * Returns the index of the last used column, or -1 if no columns are used.
   */
  public int getLastColumn() {
    return model.getColumnCount() - 1;
  }

}
