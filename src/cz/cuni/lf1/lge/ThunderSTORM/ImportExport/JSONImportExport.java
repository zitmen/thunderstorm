package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import com.json.generators.JSONGenerator;
import com.json.generators.JsonGeneratorFactory;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import ij.measure.ResultsTable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JPanel;

public class JSONImportExport implements IImportExport {

    static final String JSON_ROOT = "root";
    static final String ROOT = "results";
    
    @Override
    public void importFromFile(String fp, ResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        String inputJsonString = new Scanner(fp).useDelimiter("\\Z").next();  
        
        JsonParserFactory factory = JsonParserFactory.getInstance();
        JSONParser parser = factory.newJsonParser();
        Map jsonData = parser.parseJson(inputJsonString);

        Map rootJson = (Map)jsonData.get(JSON_ROOT);
        List al = (List)rootJson.get(ROOT);
        
        for(Object item : al) {
            Iterator it = ((Map)item).entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                rt.addValue((String)pairs.getKey(), Double.parseDouble((String)pairs.getValue()));
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    @Override
    public void exportToFile(String fp, ResultsTable rt) throws IOException {
        int ncols = rt.getLastColumn()+1, nrows = rt.getCounter();
        String [] headers = new String[ncols];
        for(int c = 0; c < ncols; c++)
            headers[c] = rt.getColumnHeading(c);
        
        Object [] results = new Object[nrows];
        for(int r = 0; r < nrows; r++) {
            HashMap<String,Double> molecule = new HashMap<String,Double>();
            for(int c = 0; c < ncols; c++)
                molecule.put(headers[c], rt.getValueAsDouble(c,r));
            results[r] = molecule;
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

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public void readParameters() {
        //
    }

}
