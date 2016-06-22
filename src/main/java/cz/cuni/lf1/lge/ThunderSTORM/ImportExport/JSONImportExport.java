package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import ij.IJ;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class JSONImportExport implements IImportExport {

    static final String JSON_ROOT = "root";
    static final String ROOT = "results";
    
    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Vector<HashMap<String,Double>> molecules = gson.fromJson(new FileReader(fp), new TypeToken<Vector<HashMap<String,Double>>>(){}.getType());

        String [] colnames = new String[1];
        Units [] colunits = new Units[1];
        double [] values = new double[1];
        int r = 0, nrows = molecules.size();
        for(HashMap<String,Double> mol : molecules) {
            if(mol.size() != colnames.length) {
                colnames = new String[mol.size()];
                colunits = new Units[mol.size()];
                values = new double[mol.size()];
            }
            int c = 0;
            for(String label : mol.keySet().toArray(new String[0])) {
                Pair<String,Units> tmp = GenericTable.parseColumnLabel(label);
                if(MoleculeDescriptor.LABEL_ID.equals(tmp.first)) continue;
                colnames[c] = tmp.first;
                colunits[c] = tmp.second;
                values[c] = mol.get(label).doubleValue();
                if(MoleculeDescriptor.LABEL_FRAME.equals(tmp.first)) {
                    values[c] += startingFrame-1;
                }
                c++;
            }
            if(!table.columnNamesEqual(colnames)) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
            }
            if(table.isEmpty()) {
                table.setDescriptor(new MoleculeDescriptor(colnames, colunits));
            }
            //
            table.addRow(values);
            IJ.showProgress((double)(r++) / (double)nrows);
        }
        table.insertIdColumn();
        table.copyOriginalToActual();
        table.setActualState();
    }

    @Override
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        assert(columns != null);
        
        int ncols = columns.size(), nrows = table.getRowCount();

        Vector<HashMap<String, BigDecimal>> results = new Vector<HashMap<String, BigDecimal>>();
        for(int r = 0; r < nrows; r++) {
            HashMap<String, BigDecimal> molecule = new HashMap<String, BigDecimal>();
            for(int c = 0; c < ncols; c++)
                molecule.put(table.getColumnLabel(columns.get(c)), new BigDecimal(table.getValue(r ,columns.get(c))).setScale(floatPrecision, BigDecimal.ROUND_HALF_EVEN).stripTrailingZeros());
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
