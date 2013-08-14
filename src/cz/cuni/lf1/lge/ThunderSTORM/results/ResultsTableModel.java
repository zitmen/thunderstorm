package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.IValue;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

class ResultsTableModel extends AbstractTableModel implements Cloneable {

    public static final int COLUMN_NOT_FOUND = -1;
    protected Vector<Molecule> rows;
    protected MoleculeDescriptor columns;
    private int maxId;

    public ResultsTableModel(ResultsTableModel res) {
        setModelData(res);
    }
    
    protected final void setModelData(ResultsTableModel res) {
        this.rows = res.rows;
        this.columns = res.columns;
        this.maxId = res.maxId;
    }
    // -----------------------------------------------------
    
    public void setLabel(int column, String new_name, Units new_units) {
        assert(new_units != null);
        
        if (new_name != null) {
            columns.names.setElementAt(new_name, column);
        }
        columns.units.setElementAt(new_units, column);
        columns.labels.setElementAt(columns.getLabel(column, true), column);
        fireTableStructureChanged();
    }

    public void setLabel(String name, String new_name, Units new_units) {
        assert(new_units != null);
        
        int column = columns.getParamColumn(name);
        if (new_name != null) {
            columns.names.setElementAt(new_name, column);
        }
        columns.units.setElementAt(new_units, column);
        columns.getLabel(column, true);
        fireTableStructureChanged();
    }

    public void reset() {
        rows.clear();
        columns = new MoleculeDescriptor(new String[] { MoleculeDescriptor.LABEL_ID });
        maxId = 0;
        try {
            insertIdColumn();
        } catch(Exception ex) {
            assert(false) : "This was supposed to never happen due to the `reset` call!";
        }
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    public Double[] getColumnAsDoubleObjects(String columnName) {
        Double [] column = new Double[rows.size()];
        int colidx = columns.getParamIndex(columnName);
        for(int i = 0; i < column.length; i++) {
            column[i] = rows.elementAt(i).getParamAt(colidx);
        }
        return column;
    }

    public double[] getColumnAsDoubles(String columnName) {
        double [] column = new double[rows.size()];
        int colidx = columns.getParamIndex(columnName);
        for(int i = 0; i < column.length; i++) {
            column[i] = rows.elementAt(i).getParamAt(colidx);
        }
        return column;
    }

    // -----------------------------------------------------
    public ResultsTableModel() {
        rows = new Vector<Molecule>();
        columns = new MoleculeDescriptor(new String[] { MoleculeDescriptor.LABEL_ID });
        maxId = 0;
    }
    
    public int getNewId() {
        return (maxId += 1);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.getParamsCount();
    }

    // this is in fact label for column heading, which the TableView asks for this class
    @Override
    public String getColumnName(int columnIndex) {
        return getColumnLabel(columnIndex);
    }
    
    public String getColumnLabel(int columnIndex) {
        return columns.getLabel(columnIndex, false);
    }

    // this is ugly, because getColumnName method is used by JTable to recieve
    // label of a column, but IJResultsTable needs to distinguish between
    // names and labels, i.e., label is "name [units]", not just "name"
    public String getColumnRealName(int columnIndex) {
        return columns.getParamNameAt(columnIndex);
    }
    
    public String getColumnLabel(String columnName) {
        return columns.getLabel(columns.getParamColumn(columnName), false);
    }
    
    public static Pair<String,Units> parseColumnLabel(String columnLabel) {
        return MoleculeDescriptor.parseColumnLabel(columnLabel);
    }

    @Override
    public int findColumn(String columnName) {
        if(!columns.hasParam(columnName)) {
            return COLUMN_NOT_FOUND;
        }
        return columns.getParamColumn(columnName);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Double.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        //return (columnIndex > 0); // column id is not editable!
        return false;   // no cell can be edited
    }
    
    Molecule getRow(int index) {
        return rows.elementAt(index);
    }

    @Override
    public Double getValueAt(int rowIndex, int columnIndex) {
        return rows.elementAt(rowIndex).values.elementAt(columns.indices.elementAt(columnIndex));
    }

    public Double getValueAt(int rowIndex, String columnName) {
        return rows.elementAt(rowIndex).values.elementAt(columns.getParamIndex(columnName));
    }

    public void setColumnUnits(String columnName, Units new_units) {
        columns.units.setElementAt(new_units, columns.getParamColumn(columnName));
    }

    public void setColumnUnits(int columnIndex, Units new_units) {
        columns.units.setElementAt(new_units, columnIndex);
    }

    public Units getColumnUnits(String columnName) {
        return columns.units.elementAt(columns.getParamColumn(columnName));
    }

    public Units getColumnUnits(int columnIndex) {
        return columns.units.elementAt(columnIndex);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if(!getColumnClass(columnIndex).isInstance(value)) {
            throw new ClassCastException("Class of the object does not match the class of the column!");
        }
        rows.elementAt(rowIndex).setParamAt(columns.indices.elementAt(columnIndex), (Double)value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    // Note that if a molecule already has an id included in `values`,
    // a method with `id` must be called!
    public synchronized int addRow(double [] values) {
        return addRow(new Molecule(columns, values));
    }
    
    public synchronized int addRow(Molecule mol) {
        if(rows.isEmpty()) {    // if the table is empty, use descriptor from the molecule instance
            setDescriptor(mol.descriptor);
        }
        columns.validateMolecule(mol);
        rows.add(mol);
        int last = rows.size() - 1;
        fireTableRowsInserted(last, last);
        return last;
    }
    
    public void setDescriptor(MoleculeDescriptor descriptor) {
        columns = descriptor;
        for(Molecule row : rows) {
            descriptor.validateMolecule(row);
        }
        fireTableStructureChanged();
    }
    
    public MoleculeDescriptor cloneDescriptor() {
        return columns.clone();
    }
    
    public void insertIdColumn() {
        maxId = 0;
        insertColumn(0, MoleculeDescriptor.LABEL_ID, Units.UNITLESS, new IValue<Double>(){
            @Override
            public Double getValue() {
                return (double)getNewId();
            }
        });
    }
    
    public void addColumn(String name, Units units, IValue<Double> value) {
        insertColumn(getColumnCount(), name, units, value);
    }
    
    public void insertColumn(int columnIndex, String name, Units units, IValue<Double> value) {
        for(Molecule row : rows) {
            row.insertParamAt(columnIndex, name, units, value.getValue().doubleValue());
        }
        fireTableStructureChanged();
        fireTableDataChanged();
    }
    
    public void deleteColumn(String name) {
        columns.removeParam(name);
        fireTableStructureChanged();
    }

    public void deleteRow(int row) {
        rows.removeElementAt(row);
        fireTableRowsDeleted(row, row);
    }

    public Vector<String> getColumnNames() {
        return columns.names;
    }

    public boolean columnExists(int column) {
        return ((column >= 0) && (column < getColumnCount()));
    }

    public boolean columnExists(String columnName) {
        return (findColumn(columnName) != COLUMN_NOT_FOUND);
    }

    public void filterRows(boolean[] keep) {
        assert(keep.length == rows.size());
        
        Vector<Molecule> newRows = new Vector<Molecule>();
        for(int i = 0; i < keep.length; i++) {
            if(keep[i]) {
                newRows.add(rows.elementAt(i));
            }
        }
        rows = newRows;
        fireTableRowsDeleted(0, keep.length - 1);
    }

    @Override
    public ResultsTableModel clone() {
        ResultsTableModel newModel = new ResultsTableModel();
        TableModelListener[] listeners = listenerList.getListeners(TableModelListener.class);
        newModel.listenerList = new EventListenerList();
        for (int i = 0; i < listeners.length; i++) {
            newModel.listenerList.add(TableModelListener.class, listeners[i]);
        }
        newModel.columns = columns.clone();
        newModel.rows = new Vector<Molecule>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            newModel.rows.add(rows.get(i).clone());
        }
        return newModel;
    }
}