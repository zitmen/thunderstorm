package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
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
    
    public void setColumnPreferredWidth(String columnName, int width) {
        int col = findColumn(columnName);
        if(col != COLUMN_NOT_FOUND) {
            setColumnPreferredWidth(col, width);
        }
    }
    
    public void setColumnPreferredWidth(int columnIndex, int width) {
        tableWindow.getView().getColumnModel().getColumn(columnIndex).setPreferredWidth(width);
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
        tableWindow.setPreviewRenderer(null);
        tableWindow.getOperationHistoryPanel().removeAllOperations();
        tableWindow.setStatus(null);
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

    public static Pair<String,Units> parseColumnLabel(String columnLabel) {
        return TripleStateTableModel.parseColumnLabel(columnLabel);
    }

    public Double[] getColumnAsDoubleObjects(String columnName) {
        return model.getColumnAsDoubleObjects(columnName, null);
    }
    
    public Double[] getColumnAsDoubleObjects(String columnName, Units units) {
        return model.getColumnAsDoubleObjects(columnName, units);
    }

    public double[] getColumnAsDoubles(String columnName) {
        return model.getColumnAsDoubles(columnName, null);
    }
    
    public double[] getColumnAsDoubles(String columnName, Units units) {
        return model.getColumnAsDoubles(columnName, units);
    }
    
    // we usually work with model indices, but for example for row sorting
    // we still need to do the conversion
    public int convertViewRowIndexToModel(int viewRowIndex) {
        return tableWindow.getView().convertRowIndexToModel(viewRowIndex);
    }
    
    public int convertModelRowIndexToView(int modelRowIndex) {
        return tableWindow.getView().convertRowIndexToView(modelRowIndex);
    }
    
    public int convertViewColumnIndexToModel(int viewColumnIndex) {
        return tableWindow.getView().convertColumnIndexToModel(viewColumnIndex);
    }
    
    public int convertModelColumnIndexToView(int modelColumnIndex) {
        return tableWindow.getView().convertColumnIndexToView(modelColumnIndex);
    }
    
    public Molecule getRow(int index) {
        return model.getRow(index);
    }
    
    public int getNewId() {
        return model.getNewId();
    }
    
    public void insertIdColumn() {
        model.insertIdColumn();
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
    
    public Double getValue(int rowIndex, int columnIndex) {
        return model.getValueAt(rowIndex, columnIndex);
    }

    public Double getValue(int rowIndex, String columnName) {
        return model.getValueAt(rowIndex, columnName);
    }

    public synchronized int addRow(Molecule row) {
        return model.addRow(row);
    }
    
    public synchronized int addRow(double [] values) {
        return model.addRow(values);
    }
    
    public void setDescriptor(MoleculeDescriptor descriptor) {
        model.setDescriptor(descriptor);
    }

    public void deleteRow(int row) {
        model.deleteRow(row);
    }
    
    public void deleteColumn(String columnName) {
        model.deleteColumn(columnName);
    }

    public Vector<String> getColumnNames() {
        return model.getColumnNames();
    }

    public boolean columnNamesEqual(String[] names) {
        if(getRowCount() == 0) return true; // if the table is empty then the colums are assumed to be empty
        int checked = (columnExists(MoleculeDescriptor.LABEL_ID) ? 1 : 0);    // ignoring column id, if it is present in the table
        for(int i = 0; i < names.length; i++) {
            if(MoleculeDescriptor.LABEL_ID.equals(names[i])) {
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
    
    public void setColumnUnits(String columnName, Units new_units) {
        setColumnUnits(model.findColumn(columnName), new_units);
    }
    
    public void setColumnUnits(int columnIndex, Units new_units) {
        Units old_units = getColumnUnits(columnIndex);
        for(int row = 0, max = getRowCount(); row < max; row++) {
            setValueAt(old_units.convertTo(new_units, getValue(row, columnIndex)), row, columnIndex);
        }
        model.setColumnUnits(columnIndex, new_units);
    }
    
    public Units getColumnUnits(String columnName) {
        return model.getColumnUnits(columnName);
    }
    
    public Units getColumnUnits(int columnIndex) {
        return model.getColumnUnits(columnIndex);
    }
    
    public boolean isEmpty() {
        return model.rows.isEmpty();
    }

    Vector<Molecule> getData() {
        return model.rows;
    }

    public void setValueAt(Double value, int rowIndex, String columnName) {
        setValueAt(value, rowIndex, findColumn(columnName));
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

    public void fireStructureChanged() {
        model.fireTableStructureChanged();
    }

    void addNewFilter(String paramName, double greaterThan, double lessThan) {
        String formula = tableWindow.getFilterFormula().trim();
        StringBuilder sb = new StringBuilder(formula);
        if(!formula.isEmpty()) {
            sb.append(" & ");
        }
        sb.append("(");
        sb.append(paramName).append(" > ").append(greaterThan);
        sb.append(" & ");
        sb.append(paramName).append(" < ").append(lessThan);
        sb.append(")");
        tableWindow.setFilterFormula(sb.toString());
    }
}
