package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.max;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.ArrayIndexComparator;
import cz.cuni.lf1.lge.ThunderSTORM.util.IValue;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import ij.IJ;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class GenericTableModel extends AbstractTableModel implements Cloneable {

    public static final int COLUMN_NOT_FOUND = -1;
    protected Vector<Molecule> rows;
    protected MoleculeDescriptor columns;
    private int maxId;

    public GenericTableModel(GenericTableModel res) {
        setModelData(res);
    }
    
    protected final void setModelData(GenericTableModel res) {
        this.rows = res.rows;
        this.columns = res.columns;
        this.maxId = res.maxId;
    }
    // -----------------------------------------------------
    
    public void sortTableByColumn(String colname) {
        if(!columnExists(colname)){
            return;
        }
        ArrayIndexComparator cmp = new ArrayIndexComparator(getColumnAsDoubles(colname, Units.UNITLESS));
        Integer indices [] = cmp.createIndexArray();
        Arrays.sort(indices, cmp);
        Molecule [] sorted = new Molecule[rows.size()];
        for(int i = 0; i < indices.length; i++) {
            sorted[i] = rows.elementAt(indices[i].intValue());
        }
        rows.clear();
        rows.addAll(Arrays.asList(sorted));
    }
    
    public void setLabel(int column, String new_name, Units new_units) {
        assert(new_units != null);
        
        if (new_name != null) {
            columns.setColumnName(column, new_name);
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

    public Double[] getColumnAsDoubleObjects(String columnName, Units units) {
        Double [] column = new Double[rows.size()];
        int colidx = columns.getParamIndex(columnName);
        if(units == null) {
            for(int i = 0; i < column.length; i++) {
                column[i] = rows.elementAt(i).getParamAt(colidx);
            }
        } else {
            Units src = columns.units.elementAt(columns.getParamColumn(columnName));
            for(int i = 0; i < column.length; i++) {
                column[i] = src.convertTo(units, rows.elementAt(i).getParamAt(colidx));
            }
        }
        return column;
    }

    public double[] getColumnAsDoubles(String columnName, Units units) {
        double [] column = new double[rows.size()];
        int colidx = columns.getParamIndex(columnName);
        if(units == null) {
            for(int i = 0; i < column.length; i++) {
                column[i] = rows.elementAt(i).getParamAt(colidx);
            }
        } else {
            Units src = columns.units.elementAt(columns.getParamColumn(columnName));
            for(int i = 0; i < column.length; i++) {
                column[i] = src.convertTo(units, rows.elementAt(i).getParamAt(colidx));
            }
        }
        return column;
    }

    // -----------------------------------------------------
    public GenericTableModel() {
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
        return (columnIndex > 0); // column id is not editable!
    }
    
    Molecule getRow(int index) {
        return rows.elementAt(index);
    }

    @Override
    public Double getValueAt(int rowIndex, int columnIndex) {
        return rows.elementAt(rowIndex).values[columns.indices.elementAt(columnIndex)];
    }

    public Double getValueAt(int rowIndex, String columnName) {
        return rows.elementAt(rowIndex).values[columns.getParamIndex(columnName)];
    }

    public void setColumnUnits(String columnName, Units new_units) {
        setColumnUnits(columns.getParamColumn(columnName), new_units);
    }

    public void setColumnUnits(int columnIndex, Units new_units) {
        setColUnitsImpl(columnIndex, new_units);
        fireTableStructureChanged();
    }

    private void setColUnitsImpl(int columnIndex, Units new_units) {
        MoleculeDescriptor.Units old_units = getColumnUnits(columnIndex);
        if(old_units.equals(new_units)) {
            return;
        }
        for(int row = 0, max = getRowCount(); row < max; row++) {
            setValImpl(old_units.convertTo(new_units, getValueAt(row, columnIndex)), row, columnIndex);
        }
        columns.setColumnUnits(new_units, columnIndex);
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
        setValImpl((Double)value, rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    private void setValImpl(double value, int rowIndex, int columnIndex) {
        rows.elementAt(rowIndex).setParamAt(columns.indices.elementAt(columnIndex), value);
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
        mol.descriptor = columns;
        rows.add(mol);
        if(mol.hasParam(MoleculeDescriptor.LABEL_ID)) {
            maxId = (int)max(maxId, mol.getParam(MoleculeDescriptor.LABEL_ID));
        }
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
            row.insertParamAt(columnIndex, name, units, value.getValue());
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
    public GenericTableModel clone() {
        GenericTableModel newModel = new GenericTableModel();
        TableModelListener[] listeners = listenerList.getListeners(TableModelListener.class);
        newModel.listenerList = new EventListenerList();
        for (int i = 0; i < listeners.length; i++) {
            newModel.listenerList.add(TableModelListener.class, listeners[i]);
        }
        newModel.columns = columns.clone();
        newModel.rows = new Vector<Molecule>();
        for(int i = 0; i < rows.size(); i++) {
            newModel.rows.add(rows.elementAt(i).clone(newModel.columns));
        }
        newModel.maxId = maxId;
        return newModel;
    }
    
    // ------------------------------------------------------------------------
    
    public void convertAllColumnsToAnalogUnits() {
        for(String colName : getColumnNames()) {
            Units columnUnits = getColumnUnits(colName);
            Units analogUnits = Units.getAnalogUnits(columnUnits);
            if(!columnUnits.equals(analogUnits)){
                setColUnitsImpl(columns.getParamColumn(colName), analogUnits);
            }
            fireTableStructureChanged();
        }
    }
    
    public void convertAllColumnsToDigitalUnits() {
        for(String colName : getColumnNames()) {
            Units columnUnits = getColumnUnits(colName);
            Units digitalUnits = Units.getDigitalUnits(columnUnits);
            if(!columnUnits.equals(digitalUnits)){
                setColUnitsImpl(columns.getParamColumn(colName), digitalUnits);
            }
            fireTableStructureChanged();
        }
    }

    public void calculateUncertaintyXY() throws MoleculeDescriptor.Fitting.UncertaintyNotApplicableException {
        // Note: even though that the lateral uncertainty can be calculated in pixels,
        //       we choose to do it in nanometers by default setting
        String paramName = MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY;
        double paramValue;
        Molecule mol;
        for(int row = 0, max = getRowCount(); row < max; row++) {
            mol = getRow(row);
            paramValue = MoleculeDescriptor.Fitting.uncertaintyXY(mol);
            if(mol.hasParam(paramName)) {
                mol.setParam(paramName, Units.NANOMETER, paramValue);
            } else {
                mol.addParam(paramName, Units.NANOMETER, paramValue);
            }
        }
        setColumnUnits(paramName, Units.NANOMETER);
        fireTableDataChanged();
    }

    public void calculateUncertaintyZ() throws MoleculeDescriptor.Fitting.UncertaintyNotApplicableException {
        String paramName = MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z;
        double paramValue;
        Molecule mol;
        for(int row = 0, max = getRowCount(); row < max; row++) {
            mol = getRow(row);
            paramValue = MoleculeDescriptor.Fitting.uncertaintyZ(mol);
            if(mol.hasParam(paramName)) {
                mol.setParam(paramName, Units.NANOMETER, paramValue);
            } else {
                mol.addParam(paramName, Units.NANOMETER, paramValue);
            }
        }
        setColumnUnits(paramName, Units.NANOMETER);
        fireTableDataChanged();
    }
}