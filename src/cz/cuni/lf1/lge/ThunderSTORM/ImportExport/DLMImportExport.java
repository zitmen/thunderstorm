package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import au.com.bytecode.opencsv.CSVReader;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.IJ;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
        
        CSVReader csvReader = new CSVReader(new FileReader(fp), separator);
        List<String[]> lines;
        lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) return;
        if(lines.get(0).length < 2) return; // header + at least 1 record!
        
        String [] colnames = new String[lines.get(0).length];
        String [] colunits = new String[lines.get(0).length];
        for(int c = 0, cm = lines.get(0).length; c < cm; c++) {
            String [] tmp = IJResultsTable.parseColumnLabel(lines.get(0)[c]);
            colnames[c] = tmp[0];
            colunits[c] = tmp[1];
        }
        if(!rt.columnNamesEqual(colnames)) {
            throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + IJResultsTable.COLUMN_ID + "')!");
        }
        
        for(int r = 1, rm = lines.size(); r < rm; r++) {
            rt.addRow();
            for(int c = 0, cm = lines.get(r).length; c < cm; c++) {
              if(IJResultsTable.COLUMN_ID.equals(colnames[c])) continue;
              rt.addValue(Double.parseDouble(lines.get(r)[c]), colnames[c]);
            }
            IJ.showProgress((double)r / (double)rm);
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
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        for(int c = 0, cm = columns.size(); c < cm; c++) {
            if(c > 0) writer.write(",");
            writer.write("\"" + rt.getColumnLabel(columns.elementAt(c)) + "\"");
        }
        writer.newLine();
        
        int ncols = columns.size(), nrows = rt.getRowCount();
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++) {
                if(c > 0) writer.write(",");
                writer.write(Double.toString(rt.getValue(r, columns.elementAt(c))));
            }
            writer.newLine();
            IJ.showProgress((double)r / (double)nrows);
        }
        
        writer.close();
    }

}
