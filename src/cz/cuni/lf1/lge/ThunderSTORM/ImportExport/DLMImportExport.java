package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

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
    public void exportToFile(String fp, IJResultsTable.View rt, Vector<String> columns) throws IOException {
        assert(rt != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        for(int c = 0, cm = columns.size(); c < cm; c++) {
            if(c > 0) writer.write(",");
            writer.write("\"" + columns.elementAt(c) + "\"");
        }
        writer.newLine();
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++) {
                if(c > 0) writer.write(",");
                writer.write(Double.toString(rt.getValue(columns.elementAt(c),r)));
            }
            writer.newLine();
            IJ.showProgress((double)r / (double)nrows);
        }
        
        writer.close();
    }

}
