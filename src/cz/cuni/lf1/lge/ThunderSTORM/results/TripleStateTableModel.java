package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Vector;

/**
 * 
 */
public class TripleStateTableModel extends ResultsTableModel {

  EnumMap<StateName, SavedState> savedStates = new EnumMap<StateName, SavedState>(StateName.class);
  StateName selectedState;

  TripleStateTableModel() {
    super();
    selectedState = StateName.ORIGINAL;
    saveSelectedState();
    savedStates.put(StateName.UNDO, savedStates.get(StateName.ORIGINAL));
    savedStates.put(StateName.ACTUAL, savedStates.get(StateName.ORIGINAL).clone());
    selectedState = StateName.ACTUAL;
    applySelectedState();
  }

  public void setSelectedState(StateName s) {
    if (selectedState == s) {
      return;
    }
    saveSelectedState();
    this.selectedState = s;
    applySelectedState();
    fireTableStructureChanged();
  }

  public StateName getSelectedState() {
    return selectedState;
  }

  public void copyActualToUndo() {
    if (selectedState == StateName.ACTUAL) {
      saveSelectedState();
    }
    savedStates.put(StateName.UNDO, savedStates.get(StateName.ACTUAL).clone());
    if (selectedState == StateName.UNDO) {
      applySelectedState();
      fireTableStructureChanged();
    }
  }

  public void copyUndoToActual() {
    if (selectedState == StateName.UNDO) {
      saveSelectedState();
    }
    savedStates.put(StateName.ACTUAL, savedStates.get(StateName.UNDO).clone());
    if (selectedState == StateName.ACTUAL) {
      applySelectedState();
      fireTableStructureChanged();
    }
  }

  public void copyOriginalToActual() {
    if (selectedState == StateName.ORIGINAL) {
      saveSelectedState();
    }
    savedStates.put(StateName.ACTUAL, savedStates.get(StateName.ORIGINAL).clone());
    savedStates.put(StateName.UNDO, savedStates.get(StateName.ORIGINAL));
    if (selectedState == StateName.ACTUAL) {
      applySelectedState();
      fireTableStructureChanged();
    }
  }

  public void swapUndoAndActual() {
    if (selectedState == StateName.UNDO || selectedState == StateName.ACTUAL) {
      saveSelectedState();
    }
    SavedState pom = savedStates.get(StateName.UNDO);
    savedStates.put(StateName.UNDO, savedStates.get(StateName.ACTUAL));
    savedStates.put(StateName.ACTUAL, pom);
    if (selectedState == StateName.UNDO || selectedState == StateName.ACTUAL) {
      applySelectedState();
      fireTableStructureChanged();
    }
  }

  @Override
  public void reset() {
    super.reset();
    saveSelectedState();
  }

  public void resetAll() {
    StateName s = selectedState;
    selectedState = StateName.ORIGINAL;
    reset();
    selectedState = StateName.UNDO;
    reset();
    selectedState = StateName.ACTUAL;
    reset();
    selectedState = s;
  }

  private void applySelectedState() {
    SavedState s = savedStates.get(selectedState);
    this.counter = s.counter;
    this.columns = s.columns;
    this.colnames = s.colnames;
  }

  private void saveSelectedState() {
    savedStates.put(selectedState, new SavedState(counter, columns, colnames));
  }

  public void setOriginalState() {
    setSelectedState(StateName.ORIGINAL);
  }
  
  public void setActualState() {
    setSelectedState(StateName.ACTUAL);
  }

  public enum StateName {

    ORIGINAL, UNDO, ACTUAL;
  }
  
  private class SavedState implements Cloneable {

    int counter;
    Vector<TableColumn> columns;
    HashMap<String, Integer> colnames;

    public SavedState(int counter, Vector<TableColumn> columns, HashMap<String, Integer> colnames) {
      this.counter = counter;
      this.columns = columns;
      this.colnames = colnames;
    }

    public SavedState() {
    }

    @Override
    protected SavedState clone() {
      SavedState newData = new SavedState();
      newData.counter = counter;
      newData.colnames = new HashMap<String, Integer>(colnames);
      newData.columns = new Vector<TableColumn>(columns.size());
      for (int i = 0; i < columns.size(); i++) {
        newData.columns.add(columns.get(i).clone());
      }
      return newData;
    }
  }
}
