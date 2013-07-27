package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import java.util.Vector;

/**
 * Class similar to ImageJ's ResultsTable class containing some of the most frequently used methods.
 * 
 * Note that all the deprecated methods were omitted. Also the methods load/save are
 * not present here - use IImportExport instead.
 * 
 * Also methods incrementCounter and getCounter are not used since it is useless.
 * In the ImageJ they are used for reallocation of memory, but here ve use collections
 * so wee don't need this functionality.
 * 
 * We also do not need to use row labels for anything, hence the related methods
 * are not implemented in this class.
 */
public class IJResultsTable {

  public static final int COLUMN_NOT_FOUND = ResultsTableModel.COLUMN_NOT_FOUND;  // -1
  public static final int COLUMN_IN_USE = -2;
  
  private static IJResultsTable resultsTable = null;
  
  /** Returns the ResultsTable used by the Measure command. This
      table must be displayed in the "Results" window. */
  public static IJResultsTable getResultsTable() {
    if(resultsTable == null) {
      setResultsTable(new IJResultsTable());
    }
    return resultsTable;
  }
  
  public static void setResultsTable(IJResultsTable rt) {
    resultsTable = rt;
  }
  
  public static boolean isResultsWindow() {
    if(resultsTable == null) return false;
    return resultsTable.table.isVisible();
  }
  
  private JavaTableWindow table;
  private ResultsTableModel results;
  public View view;
  
  /** Constructs an empty ResultsTable with the counter=0 and no columns. */
  public IJResultsTable() {
    table = new JavaTableWindow();
    results = table.getModel();
    view = new View();
  }
  
  /** Displays the contents of this ResultsTable in a window with 
      the default title "ThunderSTORM: Results", or updates an
      existing results window. Opens a new window if there is
      no open results window. */
  public void show() {
    table.show();
  }
  
  /** Displays the contents of this ResultsTable in a window with 
      the specified title, or updates an existing results window. Opens
      a new window if there is no open results window. */
  public void show(String windowTitle) {
    table.show(windowTitle);
  }
  
  public void setPreviewRenderer(RenderingQueue renderer){
    table.setPreviewRenderer(renderer);
  }
  
  /** Increments the measurement counter by one. */
  public synchronized void addRow() {
    results.addRow();
  }
  
  /** Adds a value to the end of the given column. */
  public void addValue(int column, double value) {
    results.addValue(new Double(value), column);
  }
	
	/** Adds a value to the end of the given column. If the column does not exist, it is created. */
  public void addValue(String column, double value) {
    results.addValue(new Double(value), column);
  }
  
	/** Returns a copy of the given column as a float array,
		or null if the column is empty. */
	public float[] getColumn(int column) {
    Vector<Double> vec = results.getColumnAsVector(column);
    if(vec == null) {
      return null;
    }
    
    float [] arr = new float[vec.size()];
    for(int i = 0; i < arr.length; i++) {
      arr[i] = vec.elementAt(i).floatValue();
    }
    return arr;
  }
	
  /** Returns a copy of the given column as an array of Double,
		or null if the column is empty. */
	public Double[] getColumnAsDoubleObjects(int column) {
    return results.getColumnAsArray(column);
  }
  
	/** Returns a copy of the given column as a double array,
		or null if the column is empty. */
	public double[] getColumnAsDoubles(int column) {
    Vector<Double> vec = results.getColumnAsVector(column);
    if(vec == null) {
      return null;
    }
    
    double [] arr = new double[vec.size()];
    for(int i = 0; i < arr.length; i++) {
      arr[i] = vec.elementAt(i).doubleValue();
    }
    return arr;
  }
	
	/** Returns true if the specified column exists and is not empty. */
	public boolean columnExists(int column) {
    return ((column >= 0) && (column < results.getColumnCount()));
  }
  
  public boolean columnExists(String column) {
    return (results.findColumn(column) != ResultsTableModel.COLUMN_NOT_FOUND);
  }

	/** Returns the index of the first column with the given heading.
		heading. If not found, returns COLUMN_NOT_FOUND. */
	public int getColumnIndex(String heading) {
    return results.findColumn(heading);
  }
	
	/** Sets the heading of the the first available column and
		returns that column's index. Returns COLUMN_IN_USE
		 if this is a duplicate heading. */
	public int getFreeColumn(String heading) {
    if(columnExists(heading)) {
      return COLUMN_IN_USE;
    }
    int col = results.getColumnCount();
    results.addColumn(heading, Double.class);
    return col;
  }
  
  /**	Returns the Double value of the given column and row, where
		column must be less than or equal the value returned by
		getLastColumn() and row must be greater than or equal
		zero and less than the value returned by getRowCount(). */
	public Double getValueAsDoubleObject(int column, int row) {
    return (Double)results.getValueAt(row, column);
  }
  
	/**	Returns the double value of the given column and row, where
		column must be less than or equal the value returned by
		getLastColumn() and row must be greater than or equal
		zero and less than the value returned by getRowCount(). */
	public double getValueAsDouble(int column, int row) {
    return getValueAsDoubleObject(column, row).doubleValue();
  }
	
	/**	Returns the value of the specified column and row, where
		column is the column heading and row is a number greater
		than or equal zero and less than value returned by getRowCount(). 
		Throws an IllegalArgumentException if this ResultsTable
		does not have a column with the specified heading. */
	public double getValue(String column, int row) {
    if(!columnExists(column)) {
      throw new IllegalArgumentException("Column `" + column + "` does not exist!");
    }
    return getValueAsDouble(row, results.findColumn(column));
  }

  public int getRowCount() {
    return results.getRowCount();
  }
  
  public int getColumnCount() {
    return results.getColumnCount();
  }

	/** Sets the value of the given column and row, where
		where 0&lt;=row&lt;row_count. If the specified column does 
		not exist, it is created. When adding columns, 
		<code>show()</code> must be called to update the 
		window that displays the table.*/
	public void setValue(String column, int row, double value) {
    if(column == null) {
			throw new IllegalArgumentException("Column must not be null!");
    }
		int col = getColumnIndex(column);
		if(col == COLUMN_NOT_FOUND) {
			col = getFreeColumn(column);
		}
		setValue(col, row, value);
	}

	/** Sets the value of the given column and row, where
		where 0&lt;=column&lt;=(lastRow+1 and 0&lt;=row&lt;=rowCount. */
	public void setValue(int column, int row, double value) {
    if((column < 0) || (column >= getColumnCount())) {
			throw new IllegalArgumentException("Column out of range!");
    }
		if(row >= getRowCount()) {
			if(row == getRowCount()) {
				addValue(column, value);
      } else {
				throw new IllegalArgumentException("row > counter");
      }
		} else {
      results.setValueAt(new Double(value), row, column);
    }
	}

	/** Returns the heading of the specified column or null if the column is empty. */
	public String getColumnHeading(int column) {
    if((column >= 0) && (column < results.getColumnCount())) {
      return results.getColumnName(column);
    }
    return null;
  }
  
  /** Returns a tab or comma delimited string containing the column headings. */
	public String getColumnHeadings() {
    StringBuilder builder = new StringBuilder();
		for(String heading : results.getColumnNames()) {
      builder.append(heading);
      builder.append(',');
    }
    builder.deleteCharAt(builder.length()-1);
    return builder.toString();
	}
  
	/** Deletes the specified row. */
	public synchronized void deleteRow(int row) {
    results.deleteRow(row);
  }
  
	public synchronized void reset() {
    results.reset();
  }
  
	/** Returns the index of the last used column, or -1 if no columns are used. */
	public int getLastColumn() {
    return results.getColumnCount()-1;
  }
  
  /**
   * The following API dows not access directly to the underlying ResultsModel,
   * but recieves indices from the JTable view first and then goes works with the model.
   */
  public class View {

    /** Returns a copy of the given column as an array of Double,
          or null if the column is empty. */
    public Double[] getColumnAsDoubleObjects(int column) {
      int c = resultsTable.table.getView().convertColumnIndexToModel(column);
      int rows = getRowCount();
      int [] indices = new int[rows];
      for(int i = 0; i < rows; i++) {
          indices[i] = resultsTable.table.getView().convertRowIndexToModel(i);
      }
      Vector<Double> vec = results.getColumnAsVector(c, indices);
      if(vec == null) {
        return null;
      }
      Double [] arr = new Double[vec.size()];
      return vec.toArray(arr);
    }

    /** Returns a copy of the given column as a float array,
          or null if the column is empty. */
    public float[] getColumn(int column) {
      Double [] col = getColumnAsDoubleObjects(column);
      if(col == null) return null;
      float [] res = new float[col.length];
      for(int i = 0; i < col.length; i++) {
          res[i] = col[i].floatValue();
      }
      return res;
    }

    /** Returns a copy of the given column as a double array,
          or null if the column is empty. */
    public double[] getColumnAsDoubles(int column) {
      Double [] col = getColumnAsDoubleObjects(column);
      if(col == null) return null;
      double [] res = new double[col.length];
      for(int i = 0; i < col.length; i++) {
          res[i] = col[i].doubleValue();
      }
      return res;
    }

    /** Returns true if the specified column exists and is not empty. */
    public boolean columnExists(int column) {
      return ((column >= 0) && (column < results.getColumnCount()));
    }

    public boolean columnExists(String column) {
      return (results.findColumn(column) != ResultsTableModel.COLUMN_NOT_FOUND);
    }

    /** Returns the index of the first column with the given heading.
        heading. If not found, returns COLUMN_NOT_FOUND. */
    public int getColumnIndex(String heading) {
      int col = results.findColumn(heading);
      return resultsTable.table.getView().convertColumnIndexToModel(col);
    }

    /** Sets the heading of the the first available column and
        returns that column's index. Returns COLUMN_IN_USE
        if this is a duplicate heading. */
    public int getFreeColumn(String heading) {
      if(columnExists(heading)) {
        return COLUMN_IN_USE;
      }
      int col = results.getColumnCount();
      results.addColumn(heading, Double.class);
      return col;
    }

    /** Returns the Double value of the given column and row, where
        column must be less than or equal the value returned by
        getLastColumn() and row must be greater than or equal
        zero and less than the value returned by getRowCount(). */
    public Double getValueAsDoubleObject(int column, int row) {
      int c = resultsTable.table.getView().convertColumnIndexToModel(column);
      int r = resultsTable.table.getView().convertRowIndexToModel(row);
      return (Double)results.getValueAt(r, c);
    }

    /** Returns the double value of the given column and row, where
        column must be less than or equal the value returned by
        getLastColumn() and row must be greater than or equal
        zero and less than the value returned by getRowCount(). */
    public double getValueAsDouble(int column, int row) {
      return getValueAsDoubleObject(column, row).doubleValue();
    }

    /** Returns the value of the specified column and row, where
        column is the column heading and row is a number greater
        than or equal zero and less than value returned by getRowCount(). 
        Throws an IllegalArgumentException if this ResultsTable
        does not have a column with the specified heading. */
    public double getValue(String column, int row) {
      if(!columnExists(column)) {
        throw new IllegalArgumentException("Column `" + column + "` does not exist!");
      }
      return getValueAsDouble(results.findColumn(column), row);
    }

    public int getRowCount() {
      return resultsTable.table.getView().getRowCount();
    }

    public int getColumnCount() {
      return results.getColumnCount();
    }

    /** Returns the heading of the specified column or null if the column is empty. */
    public String getColumnHeading(int column) {
      int col = resultsTable.table.getView().convertColumnIndexToModel(column);
      if((col >= 0) && (col < results.getColumnCount())) {
        return results.getColumnName(col);
      }
      return null;
    }

    /** Returns a comma delimited string containing the column headings. */
    public String getColumnHeadings() {
      StringBuilder builder = new StringBuilder();
      String [] headings = results.getColumnNames();
      for(int i = 0, col; i < headings.length; i++) {
        col = resultsTable.table.getView().convertColumnIndexToModel(i);
        builder.append(headings[col]);
        builder.append(',');
      }
      builder.deleteCharAt(builder.length()-1);
      return builder.toString();
    }

    /** Returns the index of the last used column, or -1 if no columns are used. */
    public int getLastColumn() {
      return results.getColumnCount()-1;
    }

  }

}
