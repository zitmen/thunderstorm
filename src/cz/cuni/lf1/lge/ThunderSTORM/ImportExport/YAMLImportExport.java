package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import ij.measure.ResultsTable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.yaml.snakeyaml.Yaml;

public class YAMLImportExport implements IImportExport {

    @Override
    public void importFromFile(String fp, ResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportToFile(String fp, ResultsTable rt) throws IOException {
        // TODO
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", "Silenthand Olleander");
        data.put("race", "Human");
        data.put("traits", new String[] { "ONE_HAND", "ONE_EYE" });
        Yaml yaml = new Yaml();
        String output = yaml.dump(data);
        System.out.println(output);
        //
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "YAML";
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
