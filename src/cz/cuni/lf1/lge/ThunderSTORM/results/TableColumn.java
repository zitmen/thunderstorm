package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.Vector;

class TableColumn {

  public String label;
  public Vector<Double> data;

  public TableColumn(String label) {
    this.label = label;
    this.data = new Vector<Double>();
  }

  public TableColumn(String label, Vector<Double> data) {
    assert (data != null);

    this.label = label;
    this.data = data;
  }

  @Override
  public TableColumn clone() {
    return new TableColumn(label, (Vector<Double>)data.clone());
  }

  public void filter(boolean[] keep) {
    assert (keep.length == data.size());

    Vector<Double> newData = new Vector<Double>();
    for (int i = 0; i < keep.length; i++) {
      if (keep[i]) {
        newData.add(data.get(i));
      }
    }
    data = newData;
  }
  
  public double[] asDoubleArray(){
    double[] arr = new double[data.size()];
    for(int i = 0; i< data.size(); i++){
      arr[i] = ((Double)data.get(i)).doubleValue();
    }
    return arr;
  }
  
  public float[] asFloatArray(){
    float[] arr = new float[data.size()];
    for(int i = 0; i< data.size(); i++){
      arr[i] = ((Double)data.get(i)).floatValue();
    }
    return arr;
  }
  
  public Double[] asDoubleObjectsArray(){
    return data.toArray(new Double[0]);
  }
  
}