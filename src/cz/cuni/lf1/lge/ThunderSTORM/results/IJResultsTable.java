package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
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
     * title "ThunderSTORM: Results", or updates an existing results window.
     * Opens a new window if there is no open results window.
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
    public void resetSelectedState() {
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

    public void addColumn(String name, String units) {
        model.addColumn(name, units);
    }
    
    public void addColumn(String name) {
        model.addColumn(name, null);
    }
    
    public void addColumn(String name, String units, Vector<Double> data) {
        model.addColumn(name, units, data);
    }

    public void addColumn(String name, Vector<Double> data) {
        model.addColumn(name, null, data);
    }

    public void addValue(Double value, int columnIndex) {
        model.addValue(value, columnIndex);
    }

    public void addValue(Double value, String columnName) {
        model.addValue(value, columnName);
    }
    
    public static String [] parseColumnLabel(String columnLabel) {
        return TableColumn.parseLabel(columnLabel);
    }

    public Vector<Double> getColumnAsVector(int columnIndex, int[] indices) {
        return model.getColumnAsVector(columnIndex, indices);
    }

    public Vector<Double> getColumnAsVector(int columnIndex) {
        return model.getColumnAsVector(columnIndex);
    }

    public Vector<Double> getColumnAsVector(String columnName) {
        return model.getColumnAsVector(columnName);
    }

    public Double[] getColumnAsDoubleObjects(int columnIndex) {
        return model.getColumnAsDoubleObjects(columnIndex);
    }

    public Double[] getColumnAsDoubleObjects(String columnName) {
        return model.getColumnAsDoubleObjects(columnName);
    }

    public double[] getColumnAsDoubles(int index) {
        return model.getColumnAsDoubles(index);
    }

    public double[] getColumnAsDoubles(String columnName) {
        return model.getColumnAsDoubles(columnName);
    }

    public float[] getColumnAsFloats(int index) {
        return model.getColumnAsFloats(index);
    }

    public float[] getColumnAsFloats(String columnName) {
        return model.getColumnAsFloats(columnName);
    }

    public int getRowCount() {
        return model.getRowCount();
    }

    public int getColumnCount() {
        return model.getColumnCount();
    }

    public String getColumnName(int columnIndex) {
        return model.getColumnRealName(columnIndex);
    }
    
    public String getColumnLabel(String columnName) {
        return model.getColumnLabel(columnName);
    }
    
    public String getColumnLabel(int columnIndex) {
        return model.getColumnLabel(columnIndex);
    }

    public Double getValueAt(int rowIndex, int columnIndex) {
        return model.getValueAt(rowIndex, columnIndex);
    }

    public Double getValue(int rowIndex, String columnName) {
        return model.getValue(rowIndex, columnName);
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

    public boolean columnNamesEqual(String[] names) {
        if(getRowCount() == 0) return true; // if the table is empty then the colums are assumed to be empty
        int checked = 1;    // ignoring column id
        for(int i = 0; i < names.length; i++) {
            if(PSFInstance.LABEL_ID.equals(names[i])) {
                continue;  // ignoring column id
            }
            if(columnExists(names[i])) {
                checked++;
            } else {
                return false;
            }
        }
        return (checked == getColumnCount());
    }

    public boolean columnExists(int column) {
        return model.columnExists(column);
    }

    public boolean columnExists(String columnName) {
        return model.columnExists(columnName);
    }

    public void filterRows(boolean[] keep) {
        model.filterRows(keep);
    }

    public int findColumn(String columnName) {
        return model.findColumn(columnName);
    }
    
    public void setColumnUnits(String columnName, String new_units) {
        model.setColumnUnits(columnName, new_units);
    }
    
    public void setColumnUnits(int columnIndex, String new_units) {
        model.setColumnUnits(columnIndex, new_units);
    }
    
    public String getColumnUnits(String columnName) {
        return model.getColumnUnits(columnName);
    }
    
    public String getColumnUnits(int columnIndex) {
        return model.getColumnUnits(columnIndex);
    }

    public void setValueAt(Double value, int rowIndex, String columnName) {
        model.setValueAt(value, rowIndex, columnName);
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
