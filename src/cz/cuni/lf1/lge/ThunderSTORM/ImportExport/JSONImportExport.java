package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

public class JSONImportExport implements IImportExport {

    static final String JSON_ROOT = "root";
    static final String ROOT = "results";
    
    @Override
    public void importFromFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Vector<HashMap<String,Double>> molecules = gson.fromJson(new FileReader(fp), new TypeToken<Vector<HashMap<String,Double>>>(){}.getType());

        int r = 0, nrows = molecules.size();
        for(HashMap<String,Double> mol : molecules) {
            rt.addRow();
            for(Entry<String,Double> entry : mol.entrySet()) {
                if(IJResultsTable.COLUMN_ID.equals(entry.getKey())) continue;
                rt.addValue(entry.getKey(), entry.getValue().doubleValue());
                IJ.showProgress((double)(r++) / (double)nrows);
            }
        }
    }

    @Override
    public void exportToFile(String fp, IJResultsTable.View rt, Vector<String> columns) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        String [] headers = new String[ncols];
        columns.toArray(headers);
        
        Vector<HashMap<String, Double>> results = new Vector<HashMap<String,Double>>();
        for(int r = 0; r < nrows; r++) {
            HashMap<String,Double> molecule = new HashMap<String,Double>();
            for(int c = 0; c < ncols; c++)
                molecule.put(headers[c], rt.getValue(headers[c],r));
            results.add(molecule);
            IJ.showProgress((double)r / (double)nrows);
        }

        FileWriter fw = new FileWriter(fp);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(results, new TypeToken<Vector<HashMap<String,Double>>>(){}.getType(), fw);
        fw.flush();
        fw.close();
    }

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getSuffix() {
        return "json";
    }

}
