package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.EnumMap;

public class TripleStateTableModel extends GenericTableModel {

    public static enum StateName {
        ORIGINAL, UNDO, ACTUAL;
    }

    EnumMap<StateName, GenericTableModel> savedStates = new EnumMap<StateName, GenericTableModel>(StateName.class);
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
        if(selectedState == s) {
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
        if(selectedState == StateName.ACTUAL) {
            saveSelectedState();
        }
        savedStates.put(StateName.UNDO, savedStates.get(StateName.ACTUAL).clone());
        if(selectedState == StateName.UNDO) {
            applySelectedState();
            fireTableStructureChanged();
        }
    }

    public void copyUndoToActual() {
        if(selectedState == StateName.UNDO) {
            saveSelectedState();
        }
        savedStates.put(StateName.ACTUAL, savedStates.get(StateName.UNDO).clone());
        if(selectedState == StateName.ACTUAL) {
            applySelectedState();
            fireTableStructureChanged();
        }
    }

    public void copyOriginalToActual() {
        if(selectedState == StateName.ORIGINAL) {
            saveSelectedState();
        }
        savedStates.put(StateName.ACTUAL, savedStates.get(StateName.ORIGINAL).clone());
        savedStates.put(StateName.UNDO, savedStates.get(StateName.ORIGINAL));
        if(selectedState == StateName.ACTUAL) {
            applySelectedState();
            fireTableStructureChanged();
        }
    }

    public void swapUndoAndActual() {
        if(selectedState == StateName.UNDO || selectedState == StateName.ACTUAL) {
            saveSelectedState();
        }
        GenericTableModel pom = savedStates.get(StateName.UNDO);
        savedStates.put(StateName.UNDO, savedStates.get(StateName.ACTUAL));
        savedStates.put(StateName.ACTUAL, pom);
        if(selectedState == StateName.UNDO || selectedState == StateName.ACTUAL) {
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
        setModelData(savedStates.get(selectedState));
    }

    private void saveSelectedState() {
        savedStates.put(selectedState, new GenericTableModel(this));
    }

    public void setOriginalState() {
        setSelectedState(StateName.ORIGINAL);
    }

    public void setActualState() {
        setSelectedState(StateName.ACTUAL);
    }
}
