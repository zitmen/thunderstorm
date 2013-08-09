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
        
        Yaml yaml = new Yaml();
        ArrayList<HashMap<String,Double>> molecules = (ArrayList<HashMap<String,Double>>)yaml.load(new FileReader(fp));
        
        String [] colnames = new String[1];
        String [] colunits = new String[1];
        Double [] values = new Double[1];
        int r = 0, nrows = molecules.size();
        for(HashMap<String,Double> mol : molecules) {
            if(mol.size() != colnames.length) {
                colnames = new String[mol.size()];
                colunits = new String[mol.size()];
                values = new Double[mol.size()];
            }
            int c = 0;
            for(String label : mol.keySet().toArray(new String[0])) {
                String [] tmp = IJResultsTable.parseColumnLabel(label);
                colnames[c] = tmp[0];
                colunits[c] = tmp[1];
                values[c] = mol.get(label);
                c++;
            }
            if(!rt.columnNamesEqual(colnames)) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + IJResultsTable.COLUMN_ID + "')!");
            }
            //
            rt.addRow();
            for(int i = 0; i < colnames.length; i++) {
                if(IJResultsTable.COLUMN_ID.equals(colnames[i])) continue;
                rt.addValue(values[i], colnames[i]);
                IJ.showProgress((double)(r++) / (double)nrows);
            }
        }
        for(int c = 0; c < colnames.length; c++) {
            rt.setColumnUnits(colnames[c], colunits[c]);
        }
        rt.copyOriginalToActual();
        rt.setActualState();
    }

    @Override
    public void exportToFile(String fp, IJResultsTable rt, Vector<String> columns) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        
        ArrayList<HashMap<String, Double>> results = new ArrayList<HashMap<String,Double>>();
        for(int r = 0; r < nrows; r++) {
            HashMap<String,Double> molecule = new HashMap<String,Double>();
            for(int c = 0; c < ncols; c++)
                molecule.put(rt.getColumnLabel(columns.elementAt(c)), rt.getValue(r, columns.elementAt(c)));
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

    @Override
    public String getSuffix() {
        return "yaml";
    }

}
