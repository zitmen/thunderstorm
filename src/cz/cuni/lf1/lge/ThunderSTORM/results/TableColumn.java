package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.Vector;

class TableColumn {

    public String name;
    public String units;
    public Vector<Double> data;

    public TableColumn(String name) {
        assert(name != null);

        this.name = name;
        this.units = null;
        this.data = new Vector<Double>();
    }

    public TableColumn(String name, String units) {
        assert(name != null);
        
        this.name = name;
        this.units = units;
        this.data = new Vector<Double>();
    }

    public TableColumn(String name, Vector<Double> data) {
        assert(name != null);
        assert(data != null);

        this.name = name;
        this.units = null;
        this.data = data;
    }

    public TableColumn(String name, String units, Vector<Double> data) {
        assert(name != null);
        assert(data != null);

        this.name = name;
        this.units = units;
        this.data = data;
    }
    
    public String getLabel() {
        if((units == null) || units.trim().isEmpty()) {
            return name;
        } else {
            return name + " [" + units + "]";
        }
    }
    
    public static String [] parseLabel(String label) {
        assert(label != null);
        
        int start = label.lastIndexOf('['), end = label.lastIndexOf(']');
        if((start < 0) || (end < 0) || (start > end)) {
            return new String[] { label, null };
        } else {
            return new String[] { label.substring(0, start).trim(), label.substring(start+1, end).trim() };
        }
    }

    @Override
    public TableColumn clone() {
        return new TableColumn(name, units, (Vector<Double>) data.clone());
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

    public double[] asDoubleArray() {
        double[] arr = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            arr[i] = ((Double) data.get(i)).doubleValue();
        }
        return arr;
    }

    public float[] asFloatArray() {
        float[] arr = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            arr[i] = ((Double) data.get(i)).floatValue();
        }
        return arr;
    }

    public Double[] asDoubleObjectsArray() {
        return data.toArray(new Double[0]);
    }
}