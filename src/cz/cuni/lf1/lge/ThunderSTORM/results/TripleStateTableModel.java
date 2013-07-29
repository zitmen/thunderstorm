package cz.cuni.lf1.lge.ThunderSTORM.results;

import static cz.cuni.lf1.lge.ThunderSTORM.results.ResultsTableModel.COLUMN_NOT_FOUND;
import java.util.EnumMap;
import java.util.Set;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class TripleStateTableModel extends AbstractTableModel {

  public enum State {

    ORIGINAL, UNDO, ACTUAL;
  }
  EnumMap<State, ResultsTableModel> stateModels = new EnumMap<State, ResultsTableModel>(State.class);
  State selectedState;

  TripleStateTableModel(ResultsTableModel originalModel) {
    //events of the selected model are delegated to the parent model
    originalModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        if (e.getSource() == stateModels.get(selectedState)) {
          fireTableChanged(e);
        }
      }
    });
    selectedState = State.ACTUAL;
    stateModels.put(State.ORIGINAL, originalModel);
    stateModels.put(State.UNDO, originalModel);
    stateModels.put(State.ACTUAL, originalModel.clone());
  }

  public void setSelectedState(State s) {
    this.selectedState = s;
    fireTableStructureChanged();
  }

  public State getSelectedState() {
    return selectedState;
  }

  public void copyActualToUndo() {
    stateModels.put(State.UNDO, stateModels.get(State.ACTUAL).clone());
    if (selectedState == State.UNDO) {
      fireTableStructureChanged();
    }
  }

  public void copyUndoToActual() {
    stateModels.put(State.ACTUAL, stateModels.get(State.UNDO).clone());
    if (selectedState == State.ACTUAL) {
      fireTableStructureChanged();
    }
  }

  public void copyOriginalToActual() {
    stateModels.put(State.ACTUAL, stateModels.get(State.ORIGINAL).clone());
    stateModels.put(State.UNDO, stateModels.get(State.ORIGINAL));
    if (selectedState == State.ACTUAL) {
      fireTableStructureChanged();
    }
  }

  public void swapUndoAndActual() {
    ResultsTableModel pom = stateModels.get(State.UNDO);
    stateModels.put(State.UNDO, stateModels.get(State.ACTUAL));
    stateModels.put(State.ACTUAL, pom);
    if (selectedState == State.UNDO || selectedState == State.ACTUAL) {
      fireTableStructureChanged();
    }
  }

  public void resetSelected() {
    stateModels.get(selectedState).reset();
  }

  public void resetAll() {
    stateModels.get(State.ORIGINAL).reset();
    stateModels.get(State.UNDO).reset();
    stateModels.get(State.ACTUAL).reset();
    setSelectedState(State.ACTUAL);
  }

  //---
  @Override
  public int getRowCount() {
    return stateModels.get(selectedState).getRowCount();
  }

  @Override
  public int getColumnCount() {
    return stateModels.get(selectedState).getColumnCount();
  }

  @Override
  public Double getValueAt(int rowIndex, int columnIndex) {
    return stateModels.get(selectedState).getValueAt(rowIndex, columnIndex);
  }
  
  public Double getValue(String columnName, int columnIndex){
    return stateModels.get(selectedState).getValue(columnName, columnIndex);
  }
  
  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    stateModels.get(selectedState).setValueAt(value, rowIndex, columnIndex);
  }

  public void setValueAt(Double value, int rowIndex, String columnHeading) {
    stateModels.get(selectedState).setValueAt(value, rowIndex, columnHeading);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return stateModels.get(selectedState).isCellEditable(rowIndex, columnIndex);
  }

  @Override
  public String getColumnName(int columnIndex) {
    return stateModels.get(selectedState).getColumnName(columnIndex);
  }

  @Override
  public int findColumn(String columnName) {
    return stateModels.get(selectedState).findColumn(columnName);
  }

  public boolean columnExists(int column) {
    return ((column >= 0) && (column < getColumnCount()));
  }

  public boolean columnExists(String column) {
    return (findColumn(column) != COLUMN_NOT_FOUND);
  }

  public String[] getColumnNames() {
    return stateModels.get(selectedState).getColumnNames();
  }

  public double[] getColumnAsDoubles(String columnName) {
    return stateModels.get(selectedState).getColumnAsDoubles(columnName);
  }

  public float[] getColumnAsFloats(String columnName) {
    return stateModels.get(selectedState).getColumnAsFloats(columnName);
  }

  public Double[] getColumnAsDoubleObjects(String columnLabel) {
    return stateModels.get(selectedState).getColumnAsDoubleObjects(columnLabel);
  }

  public int addRow() {
    return stateModels.get(selectedState).addRow();
  }

  public void addValue(Double value, int columnIndex) {
    stateModels.get(selectedState).addValue(value, columnIndex);
  }

  public void addValue(Double value, String columnLabel) {
    stateModels.get(selectedState).addValue(value, columnLabel);
  }

  public void filterRows(boolean[] keep) {
    stateModels.get(selectedState).filterRows(keep);
  }
}
