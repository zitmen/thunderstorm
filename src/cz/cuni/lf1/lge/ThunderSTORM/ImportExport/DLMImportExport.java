package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import ij.measure.ResultsTable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

abstract public class DLMImportExport implements IImportExport {
    
    private char separator;
    
    public DLMImportExport(char separator) {
        this.separator = separator;
    }

    @Override
    public void importFromFile(String fp, ResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        CSVReader csvReader;
        csvReader = new CSVReader(new FileReader(fp), separator);
        List<String[]> lines;
        lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) return;
        if(lines.get(0).length < 2) return; // header + at least 1 record!
        
        String header;
        for(int c = 0, cm = lines.get(0).length; c < cm; c++) {
            header = lines.get(0)[c];
            for(int r = 1, rm = lines.size(); r < rm; r++) {
                rt.addValue(header, Double.parseDouble(lines.get(r)[c]));
            }
        }
        
        rt.updateResults();
    }

    @Override
    public void exportToFile(String fp, ResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        int ncols = rt.getLastColumn()+1, nrows = rt.getCounter();
        LinkedList<String[]> lines = new LinkedList<String[]>();
        
        String [] headers = new String[ncols];
        for(int c = 0; c < ncols; c++)
            headers[c] = rt.getColumnHeading(c);
        lines.add(headers);
        
        String [] values = new String[ncols];
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++)
                values[c] = Double.toString(rt.getValueAsDouble(c,r));
            lines.add(values);
        }
        
        CSVWriter csvWriter = new CSVWriter(new FileWriter(fp), separator);
        csvWriter.writeAll(lines);
        csvWriter.close();
    }

}
