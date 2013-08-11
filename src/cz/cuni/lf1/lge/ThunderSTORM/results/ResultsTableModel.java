package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

class ResultsTableModel extends AbstractTableModel {

    public static final int COLUMN_NOT_FOUND = -1;
    protected int counter;
    protected Vector<TableColumn> columns;
    protected HashMap<String, Integer> colnames;
    // -----------------------------------------------------

    public void addColumn(String name, String units) {
        columns.add(new TableColumn(name, units));
        colnames.put(name, columns.size() - 1);
        fireTableStructureChanged();
    }

    public void addColumn(String name, String units, Vector<Double> data) {
        columns.add(new TableColumn(name, units, data));
        colnames.put(name, columns.size() - 1);
        fireTableStructureChanged();
    }

    public void setLabel(int column, String new_name, String new_units) {
        if (new_name != null) {
            columns.elementAt(column).name = new_name;
        }
        if (new_units != null) {
            columns.elementAt(column).units = new_units;
        }
        fireTableStructureChanged();
    }

    public void setLabel(String name, String new_name, String new_units) {
        setLabel(colnames.get(name).intValue(), new_name, new_units);
        fireTableStructureChanged();
    }

    public void reset() {
        counter = 0;
        columns.clear();
        colnames.clear();

        addColumn(PSFInstance.LABEL_ID, null);
        fireTableStructureChanged();
    }

    public void addValue(Double value, int columnIndex) {
        if (!getColumnClass(columnIndex).isInstance(value)) {
            throw new ClassCastException("Class of the object does not match the class of the column!");
        }
        columns.elementAt(columnIndex).data.add(value);
        fireTableCellUpdated(columns.elementAt(columnIndex).data.size() - 1, columnIndex);
    }

    public void addValue(Double value, String columnName) {
        if (findColumn(columnName) == COLUMN_NOT_FOUND) {
            addColumn(columnName, null);
        }
        addValue(value, colnames.get(columnName).intValue());
    }

    public void setValueAt(Double value, int rowIndex, String columnName) {
        getColumn(columnName).data.set(rowIndex, value);
        fireTableCellUpdated(rowIndex, findColumn(columnName));
    }

    public Vector<Double> getColumnAsVector(int columnIndex, int[] indices) {
        Vector<Double> column = columns.elementAt(columnIndex).data;
        Vector<Double> res = new Vector<Double>();
        for (int i = 0; i < indices.length; i++) {
            res.add(column.elementAt(indices[i]));
        }
        return res;
    }

    public Vector<Double> getColumnAsVector(int columnIndex) {
        return getColumn(columnIndex).data;
    }

    public Vector<Double> getColumnAsVector(String columnName) {
        return getColumn(columnName).data;
    }

    public Double[] getColumnAsDoubleObjects(int columnIndex) {
        return getColumn(columnIndex).asDoubleObjectsArray();
    }

    public Double[] getColumnAsDoubleObjects(String columnName) {
        return getColumn(columnName).asDoubleObjectsArray();
    }

    public double[] getColumnAsDoubles(int index) {
        return getColumn(index).asDoubleArray();
    }

    public double[] getColumnAsDoubles(String heading) {
        return getColumn(heading).asDoubleArray();
    }

    public float[] getColumnAsFloats(int index) {
        return getColumn(index).asFloatArray();
    }

    public float[] getColumnAsFloats(String heading) {
        return getColumn(heading).asFloatArray();
    }

    private TableColumn getColumn(int index) {
        return columns.get(index);
    }

    private TableColumn getColumn(String heading) {
        int idx = findColumn(heading);
        if (idx != COLUMN_NOT_FOUND) {
            return getColumn(idx);
        } else {
            throw new IllegalArgumentException("Column " + heading + " does not exist.");
        }
    }

    // -----------------------------------------------------
    public ResultsTableModel() {
        columns = new Vector<TableColumn>();
        colnames = new HashMap<String, Integer>();

        counter = 0;
        addColumn(PSFInstance.LABEL_ID, null);
    }

    @Override
    public int getRowCount() {
        return counter;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return getColumnLabel(columnIndex);
    }
    
    // this is ugly, because getColumnName method is used by JTable to recieve
    // label of a column, but IJResultsTable needs to distinguish between
    // names and labels, i.e., label is "name [units]", not just "name"
    public String getColumnRealName(int columnIndex) {
        return getColumn(columnIndex).name;
    }
    
    public String getColumnLabel(String columnName) {
        return getColumn(columnName).getLabel();
    }
    
    public String getColumnLabel(int columnIndex) {
        return getColumn(columnIndex).getLabel();
    }

    @Override
    public int findColumn(String columnName) {
        if (!colnames.containsKey(columnName)) {
            return COLUMN_NOT_FOUND;
        }
        return colnames.get(columnName).intValue();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Double.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0); // column id is not editable!
    }

    @Override
    public Double getValueAt(int rowIndex, int columnIndex) {
        return getColumn(columnIndex).data.elementAt(rowIndex);
    }

    public Double getValue(int rowIndex, String columnName) {
        return getColumn(columnName).data.elementAt(rowIndex);
    }

    public void setColumnUnits(String columnName, String new_units) {
        getColumn(columnName).units = new_units;
    }

    public void setColumnUnits(int columnIndex, String new_units) {
        getColumn(columnIndex).units = new_units;
    }

    public String getColumnUnits(String columnName) {
        return getColumn(columnName).units;
    }

    public String getColumnUnits(int columnIndex) {
        return getColumn(columnIndex).units;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!getColumnClass(columnIndex).isInstance(value)) {
            throw new ClassCastException("Class of the object does not match the class of the column!");
        }
        columns.elementAt(columnIndex).data.set(rowIndex, (Double) value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public synchronized int addRow() {
        counter++;
        columns.elementAt(0).data.add(new Double(counter));
        fireTableRowsInserted(counter - 1, counter - 1);
        return counter - 1;
    }

    public void deleteRow(int row) {
        for (TableColumn col : columns) {
            col.data.removeElementAt(row);
        }
        fireTableRowsDeleted(row, row);
        counter--;
    }

    public String[] getColumnNames() {
        String[] names = new String[colnames.size()];
        colnames.keySet().toArray(names);
        return names;
    }

    public boolean columnExists(int column) {
        return ((column >= 0) && (column < getColumnCount()));
    }

    public boolean columnExists(String columnName) {
        return (findColumn(columnName) != COLUMN_NOT_FOUND);
    }

    public void filterRows(boolean[] keep) {
        for (TableColumn col : columns) {
            col.filter(keep);
        }
        counter = columns.get(0).data.size();
        fireTableRowsDeleted(0, keep.length - 1);
    }

    @Override
    public ResultsTableModel clone() {
        ResultsTableModel newModel = new ResultsTableModel();
        newModel.counter = counter;
        TableModelListener[] listeners = listenerList.getListeners(TableModelListener.class);
        newModel.listenerList = new EventListenerList();
        for (int i = 0; i < listeners.length; i++) {
            newModel.listenerList.add(TableModelListener.class, listeners[i]);
        }
        newModel.colnames = new HashMap<String, Integer>(colnames);
        newModel.columns = new Vector<TableColumn>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            newModel.columns.add(columns.get(i).clone());
        }
        return newModel;
    }
}