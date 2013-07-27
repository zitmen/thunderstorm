package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class YAMLImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        Yaml yaml = new Yaml();
        ArrayList<HashMap<String,Double>> molecules = (ArrayList<HashMap<String,Double>>)yaml.load(new FileReader(fp));
        
        int r = 0, nrows = molecules.size();
        for(HashMap<String,Double> mol : molecules) {
            rt.addRow();
            for(Map.Entry<String,Double> entry : mol.entrySet()) {
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
        
        ArrayList<HashMap<String, Double>> results = new ArrayList<HashMap<String,Double>>();
        for(int r = 0; r < nrows; r++) {
            HashMap<String,Double> molecule = new HashMap<String,Double>();
            for(int c = 0; c < ncols; c++)
                molecule.put(headers[c], rt.getValue(headers[c],r));
            results.add(molecule);
            IJ.showProgress((double)r / (double)nrows);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        writer.write(yaml.dump(results));
        writer.close();
    }

    @Override
    public String getName() {
        return "YAML";
    }

}
