package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.Vector;

class TableColumn<T> {
    
    public String label;
    public Class<T> type;
    public Vector<T> data;

    public TableColumn(String label, Class<T> type) {
      this.label = label;
      this.type = type;
      this.data = new Vector<T>();
    }
    
    public TableColumn(String label, Class<T> type, Vector<T> data) {
      assert(data != null);
      
      this.label = label;
      this.type = type;
      this.data = data;
    }
    
    @Override
    public TableColumn<T> clone() {
      return new TableColumn(label, type, (Vector<T>)data.clone());
    }
  }