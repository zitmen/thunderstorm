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

  public static final String COLUMN_ID = "#";
  public static final int COLUMN_NOT_FOUND = ResultsTableModel.COLUMN_NOT_FOUND;  // -1
  public static final int COLUMN_IN_USE = -2;
  private static IJResultsTable resultsTable = null;

  /**
   * Returns the ResultsTable used by the Measure command. This table must be
   * displayed in the "Results" window.
   */
  public synchronized static IJResultsTable getResultsTable() {
    if (resultsTable == null) {
      setResultsTable(new IJResultsTable());
    }
    return resultsTable;
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

  public synchronized void reset() {
    model.resetAll();
  }
  
  //delegated methods from model
  public void resetSelectedState(){
    model.reset();
  }

  public TripleStateTableModel.StateName getSelectedState() {
    return model.getSelectedState();
  }

  public void copyActualToUndo() {
    model.copyActualToUndo();
  }

  public void copyUndoToActual() {
    model.copyUndoToActual();
  }

  public void copyOriginalToActual() {
    model.copyOriginalToActual();
  }

  public void swapUndoAndActual() {
    model.swapUndoAndActual();
  }

  public void setOriginalState() {
    model.setOriginalState();
  }

  public void setActualState() {
    model.setActualState();
  }

  public void addColumn(String label) {
    model.addColumn(label);
  }

  public void addColumn(String label, Vector<Double> data) {
    model.addColumn(label, data);
  }

  public void addValue(Double value, int columnIndex) {
    model.addValue(value, columnIndex);
  }

  public void addValue(Double value, String columnLabel) {
    model.addValue(value, columnLabel);
  }

  public Vector<Double> getColumnAsVector(int columnIndex, int[] indices) {
    return model.getColumnAsVector(columnIndex, indices);
  }

  public Vector<Double> getColumnAsVector(int columnIndex) {
    return model.getColumnAsVector(columnIndex);
  }

  public Vector<Double> getColumnAsVector(String columnLabel) {
    return model.getColumnAsVector(columnLabel);
  }

  public Double[] getColumnAsDoubleObjects(int columnIndex) {
    return model.getColumnAsDoubleObjects(columnIndex);
  }

  public Double[] getColumnAsDoubleObjects(String columnLabel) {
    return model.getColumnAsDoubleObjects(columnLabel);
  }

  public double[] getColumnAsDoubles(int index) {
    return model.getColumnAsDoubles(index);
  }

  public double[] getColumnAsDoubles(String heading) {
    return model.getColumnAsDoubles(heading);
  }

  public float[] getColumnAsFloats(int index) {
    return model.getColumnAsFloats(index);
  }

  public float[] getColumnAsFloats(String heading) {
    return model.getColumnAsFloats(heading);
  }

  public int getRowCount() {
    return model.getRowCount();
  }

  public int getColumnCount() {
    return model.getColumnCount();
  }

  public String getColumnName(int columnIndex) {
    return model.getColumnName(columnIndex);
  }

  public Double getValueAt(int rowIndex, int columnIndex) {
    return model.getValueAt(rowIndex, columnIndex);
  }

  public Double getValue(int rowIndex, String column) {
    return model.getValue(rowIndex, column);
  }

  public synchronized int addRow() {
    return model.addRow();
  }

  public void deleteRow(int row) {
    model.deleteRow(row);
  }

  public String[] getColumnNames() {
    return model.getColumnNames();
  }

  public boolean columnExists(int column) {
    return model.columnExists(column);
  }

  public boolean columnExists(String column) {
    return model.columnExists(column);
  }

  public void filterRows(boolean[] keep) {
    model.filterRows(keep);
  }

  public int findColumn(String columnName) {
    return model.findColumn(columnName);
  }

  public void setValueAt(Double value, int rowIndex, String columnLabel) {
    model.setValueAt(value, rowIndex, columnLabel);
  }

  public void setValueAt(Double value, int rowIndex, int columnIndex) {
    model.setValueAt(value, rowIndex, columnIndex);
  }
 
  //delegated methods from window
  public void showPreview() {
    tableWindow.showPreview();
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

  
}
