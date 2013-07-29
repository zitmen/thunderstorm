package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

final class ResultsTableModel extends AbstractTableModel {

  public static final int COLUMN_NOT_FOUND = -1;
  private int counter;
  private Vector<TableColumn> columns;
  private HashMap<String, Integer> colnames;
  // -----------------------------------------------------

  public void addColumn(String label) {
    columns.add(new TableColumn(label));
    colnames.put(label, columns.size() - 1);
    fireTableStructureChanged();
  }

  public void addColumn(String label, Vector<Double> data) {
    columns.add(new TableColumn(label, data));
    colnames.put(label, columns.size() - 1);
    fireTableStructureChanged();
  }

  public void setLabel(int column, String new_label) {
    columns.elementAt(column).label = new_label;
    fireTableStructureChanged();
  }

  public void setLabel(String old_label, String new_label) {
    setLabel(colnames.get(old_label).intValue(), new_label);
    fireTableStructureChanged();
  }

  public void reset() {
    counter = 0;
    columns.clear();
    colnames.clear();

    addColumn("#");

    fireTableStructureChanged();
  }

  public void addValue(Double value, int columnIndex) {
    if (!getColumnClass(columnIndex).isInstance(value)) {
      throw new ClassCastException("Class of the object does not match the class of the column!");
    }
    columns.elementAt(columnIndex).data.add(value);
    fireTableCellUpdated(columns.elementAt(columnIndex).data.size() - 1, columnIndex);
  }

  public void addValue(Double value, String columnLabel) {
    if (findColumn(columnLabel) == COLUMN_NOT_FOUND) {
      addColumn(columnLabel);
    }
    addValue(value, colnames.get(columnLabel).intValue());
  }

  public void setValueAt(Double value, int rowIndex, String columnLabel) {
    getColumn(columnLabel).data.set(rowIndex, value);
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

  public Vector<Double> getColumnAsVector(String columnLabel) {
    return getColumn(columnLabel).data;
  }

  public Double[] getColumnAsDoubleObjects(int columnIndex) {
    return getColumn(columnIndex).asDoubleObjectsArray();
  }

  public Double[] getColumnAsDoubleObjects(String columnLabel) {
    return getColumn(columnLabel).asDoubleObjectsArray();
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

    reset();
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
    return columns.elementAt(columnIndex).label;
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
    return (columnIndex > 0); // "#" column is not editable!
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return columns.elementAt(columnIndex).data.elementAt(rowIndex);
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    if (!getColumnClass(columnIndex).isInstance(value)) {
      throw new ClassCastException("Class of the object does not match the class of the column!");
    }
    columns.elementAt(columnIndex).data.set(rowIndex, (Double)value);
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  public synchronized int addRow() {
    counter++;
    columns.elementAt(0).data.add(new Double(counter));
    fireTableRowsInserted(counter - 1, counter - 1);
    return counter-1;
  }

  public void deleteRow(int row) {
    for (TableColumn col : columns) {
      col.data.removeElementAt(row);
    }
    fireTableRowsDeleted(row, row);
    counter--;
  }

  public String[] getColumnNames() {
    return colnames.keySet().toArray(new String[0]);
  }
  
  public void filterRows(boolean[] keep){
    for (TableColumn col : columns) {
      col.filter(keep);
    }
    counter = columns.get(0).data.size();
    fireTableRowsDeleted(0, keep.length-1);
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