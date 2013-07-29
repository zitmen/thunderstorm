package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import com.json.generators.JSONGenerator;
import com.json.generators.JsonGeneratorFactory;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import cz.cuni.lf1.lge.ThunderSTORM.results.TripleStateTableModel;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class JSONImportExport implements IImportExport {

    static final String JSON_ROOT = "root";
    static final String ROOT = "results";
    
    @Override
    public void importFromFile(String fp, TripleStateTableModel rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.resetAll();
        rt.setSelectedState(TripleStateTableModel.State.ORIGINAL);
        
        String inputJsonString = new Scanner(fp).useDelimiter("\\Z").next();  
        
        JsonParserFactory factory = JsonParserFactory.getInstance();
        JSONParser parser = factory.newJsonParser();
        Map jsonData = parser.parseJson(inputJsonString);

        Map rootJson = (Map)jsonData.get(JSON_ROOT);
        List al = (List)rootJson.get(ROOT);
        
        int r = 0, nrows = al.size();
        for(Object item : al) {
            rt.addRow();
            Iterator it = ((Map)item).entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                rt.addValue(Double.parseDouble((String)pairs.getValue()), (String)pairs.getKey());
                IJ.showProgress((double)(r++) / (double)nrows);
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        rt.copyOriginalToActual();
        rt.setSelectedState(TripleStateTableModel.State.ACTUAL);
    }

    @Override
    public void exportToFile(String fp, TripleStateTableModel rt) throws IOException {
        int ncols = rt.getColumnCount(), nrows = rt.getRowCount();
        String [] headers = rt.getColumnNames();
        
        Object [] results = new Object[nrows];
        for(int r = 0; r < nrows; r++) {
            HashMap<String,Double> molecule = new HashMap<String,Double>();
            for(int c = 0; c < ncols; c++)
                molecule.put(headers[c], (Double)rt.getValueAt(c,r));
            results[r] = molecule;
            IJ.showProgress((double)r / (double)nrows);
        }

        HashMap data = new HashMap();
        data.put(ROOT, results);

        JsonGeneratorFactory factory = JsonGeneratorFactory.getInstance();
        JSONGenerator generator = factory.newJsonGenerator();
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        writer.write(generator.generateJson(data));
        writer.close();
    }

    @Override
    public String getName() {
        return "JSON";
    }

}
