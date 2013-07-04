package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
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
    public void importFromFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        rt.reset();
        
        CSVReader csvReader = new CSVReader(new FileReader(fp), separator);
        List<String[]> lines;
        lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) return;
        if(lines.get(0).length < 2) return; // header + at least 1 record!
        
        String [] headers = new String[lines.get(0).length];
        for(int c = 0, cm = lines.get(0).length; c < cm; c++) {
            headers[c] = lines.get(0)[c];
        }
        
        for(int r = 1, rm = lines.size(); r < rm; r++) {
            rt.addRow();
            for(int c = 0, cm = lines.get(r).length; c < cm; c++) {
                rt.addValue(headers[c], Double.parseDouble(lines.get(r)[c]));
            }
            IJ.showProgress((double)r / (double)rm);
        }
    }

    @Override
    public void exportToFile(String fp, IJResultsTable rt) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        int ncols = rt.view.getColumnCount(), nrows = rt.view.getRowCount();
        LinkedList<String[]> lines = new LinkedList<String[]>();
        
        String [] headers = new String[ncols];
        for(int c = 0; c < ncols; c++)
            headers[c] = rt.view.getColumnHeading(c);
        lines.add(headers);
        
        String [][] values = new String[nrows][ncols];
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++)
                values[r][c] = Double.toString(rt.view.getValueAsDouble(c,r));
            lines.add(values[r]);
            IJ.showProgress((double)r / (double)nrows);
        }
        
        CSVWriter csvWriter = new CSVWriter(new FileWriter(fp), separator);
        csvWriter.writeAll(lines);
        csvWriter.close();
    }

}
