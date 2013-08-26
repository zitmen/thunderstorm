package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import java.util.Vector;

public abstract class GenericTable<TW extends GenericTableWindow> {
    
    public static final int COLUMN_NOT_FOUND = -1;
    public static final int COLUMN_IN_USE = -2;

    TW tableWindow;
    TripleStateTableModel model;
    
    /**
     * Constructs an empty ResultsTable with the counter=0 and no columns.
     */
    public GenericTable(TW tableWindow) {
        this.tableWindow = tableWindow;
        this.model = tableWindow.getModel();
        this.tableWindow.packFrame();
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

    public static Pair<String,MoleculeDescriptor.Units> parseColumnLabel(String columnLabel) {
        return GenericTableModel.parseColumnLabel(columnLabel);
    }

    public Double[] getColumnAsDoubleObjects(String columnName) {
        return model.getColumnAsDoubleObjects(columnName, null);
    }
    
    public Double[] getColumnAsDoubleObjects(String columnName, MoleculeDescriptor.Units units) {
        return model.getColumnAsDoubleObjects(columnName, units);
    }

    public double[] getColumnAsDoubles(String columnName) {
        return model.getColumnAsDoubles(columnName, null);
    }
    
    public double[] getColumnAsDoubles(String columnName, MoleculeDescriptor.Units units) {
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
    
    public MoleculeDescriptor getDescriptor() {
        return model.columns;
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
    
    public void setColumnUnits(String columnName, MoleculeDescriptor.Units new_units) {
        setColumnUnits(model.findColumn(columnName), new_units);
    }
    
    public void setColumnUnits(int columnIndex, MoleculeDescriptor.Units new_units) {
        model.setColumnUnits(columnIndex, new_units);
    }
    
    public MoleculeDescriptor.Units getColumnUnits(String columnName) {
        return model.getColumnUnits(columnName);
    }
    
    public MoleculeDescriptor.Units getColumnUnits(int columnIndex) {
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

    public void fireStructureChanged() {
        model.fireTableStructureChanged();
    }
    
    public void fireDataChanged() {
        model.fireTableDataChanged();
    }
    
    public void convertAllColumnsToAnalogUnits() {
        model.convertAllColumnsToAnalogUnits();
    }
    
    public void convertAllColumnsToDigitalUnits() {
        model.convertAllColumnsToDigitalUnits();
    }

    public void calculateThompsonFormula() {
        model.calculateThompsonFormula();
    }

    public abstract String getFrameTitle();
    public abstract String getTableIdentifier();

}
