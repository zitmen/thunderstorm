package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

final class ResultsTableModel extends AbstractTableModel {
  
  public static final int COLUMN_NOT_FOUND = -1;

  private int counter;
  private Vector<TableColumn> columns;
  private HashMap<String,Integer> colnames;
  // -----------------------------------------------------
  public <T> void addColumn(String label, Class<T> type) {
    columns.add(new TableColumn<T>(label, type));
    colnames.put(label, columns.size()-1);
    fireTableStructureChanged();
  }

  public <T> void addColumn(String label, Class<T> type, Vector<T> data) {
    columns.add(new TableColumn<T>(label, type, data));
    colnames.put(label, columns.size()-1);
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

    addColumn(IJResultsTable.COLUMN_ID, Double.class);
    
    fireTableStructureChanged();
  }

  public void addValue(Object value, int columnIndex) {
    if(!getColumnClass(columnIndex).isInstance(value))
      throw new ClassCastException("Class of the object does not match the class of the column!");
    columns.elementAt(columnIndex).data.add(value);
    fireTableCellUpdated(columns.elementAt(columnIndex).data.size()-1, columnIndex);
  }

  public void addValue(Object value, String columnLabel) {
    if(findColumn(columnLabel) == COLUMN_NOT_FOUND) {
      addColumn(columnLabel, value.getClass());
    }
    addValue(value, colnames.get(columnLabel).intValue());
  }

  public void setValueAt(Object value, int rowIndex, String columnLabel) {
    setValueAt(value, rowIndex, colnames.get(columnLabel).intValue());
  }

  public Vector<Double> getColumnAsVector(int columnIndex) {
    return columns.elementAt(columnIndex).data;
  }
  
  public Vector<Double> getColumnAsVector(int columnIndex, int [] indices) {
    Vector<Double> column = columns.elementAt(columnIndex).data;
    Vector<Double> res = new Vector<Double>();
    for(int i = 0; i < indices.length; i++) {
        res.add(column.elementAt(indices[i]));
    }
    return res;
  }

  public Vector<Double> getColumnAsVector(String columnLabel) {
    int index = findColumn(columnLabel);
    if(index == COLUMN_NOT_FOUND) {
      return null;
    } else {
      return getColumnAsVector(index);
    }
  }

  public Double[] getColumnAsArray(int columnIndex) {
    Vector<Double> vec = getColumnAsVector(columnIndex);
    Double [] arr = new Double[vec.size()];
    return vec.toArray(arr);
  }

  public Double[] getColumnAsArray(String columnLabel) {
    Vector<Double> vec = getColumnAsVector(columnLabel);
    Double [] arr = new Double[vec.size()];
    return vec.toArray(arr);
  }
  // -----------------------------------------------------
  public ResultsTableModel() {
    columns = new Vector<TableColumn>();
    colnames = new HashMap<String,Integer>();

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
    if(!colnames.containsKey(columnName)) {
      return COLUMN_NOT_FOUND;
    }
    return colnames.get(columnName).intValue();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columns.elementAt(columnIndex).type;
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
    if(!getColumnClass(columnIndex).isInstance(value))
      throw new ClassCastException("Class of the object does not match the class of the column!");
    columns.elementAt(columnIndex).data.set(rowIndex, value);
    fireTableCellUpdated(rowIndex, columnIndex);
  }
  
  public synchronized void addRow() {
    counter++;
    columns.elementAt(0).data.add(new Double(counter));
    fireTableRowsInserted(counter-1, counter-1);
  }

  public void deleteRow(int row) {
    for(TableColumn col : columns) {
      col.data.removeElementAt(row);
    }
    fireTableRowsDeleted(row, row);
    counter--;
  }
  
  public String [] getColumnNames() {
    String [] names = new String[colnames.size()];
    for(Entry<String,Integer> entry : colnames.entrySet()) {
        names[entry.getValue().intValue()] = entry.getKey();
    }
    return names;
  }
}