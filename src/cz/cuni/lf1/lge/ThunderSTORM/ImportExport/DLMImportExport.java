package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import au.com.bytecode.opencsv.CSVReader;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import ij.IJ;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.input.CountingInputStream;

abstract public class DLMImportExport implements IImportExport {
    
    private char separator;
    
    public DLMImportExport(char separator) {
        this.separator = separator;
    }

    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        File file = new File(fp);
        long fileSize = file.length();
        //im using file size and counting bytes read to track progress (without having to know line count in advance)
        CountingInputStream countingInputStream = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
        Reader fileReader = new InputStreamReader(countingInputStream);
        
        CSVReader csvReader = new CSVReader(fileReader, separator);
        String[] firstLine = csvReader.readNext();
        
        if(firstLine == null) return;
        if(firstLine.length < 2) return;
        
        Vector<Pair<String,Units>> cols = new Vector<Pair<String,Units>>();
        int c_id = -1;
        for(int c = 0, cm = firstLine.length; c < cm; c++) {
            Pair<String,Units> tmp = GenericTable.parseColumnLabel(firstLine[c]);
            if(MoleculeDescriptor.LABEL_ID.equals(tmp.first)) { c_id = c; continue; }
            cols.add(tmp);
        }
        String [] colnames = new String[cols.size()];
        Units [] colunits = new Units[cols.size()];
        for(int c = 0, cm = cols.size(); c < cm; c++) {
            colnames[c] = cols.elementAt(c).first;
            colunits[c] = cols.elementAt(c).second;
        }
        //
        if(!table.columnNamesEqual(colnames)) {
            throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
        }
        if(table.isEmpty()) {
            table.setDescriptor(new MoleculeDescriptor(colnames, colunits));
        }
        //
        double [] values = new double[colnames.length];
        String[] line;
        while((line = csvReader.readNext()) != null){
            for(int c = 0, ci = 0, cm = line.length; c < cm; c++) {
                if(c == c_id)
                    continue;
                values[ci] = Double.parseDouble(line[c]);
                if(MoleculeDescriptor.LABEL_FRAME.equals(colnames[ci])) {
                    values[ci] += startingFrame - 1;
                }
                ci++;
            }
            table.addRow(values);
            IJ.showProgress((double)countingInputStream.getByteCount() / (double)fileSize);
        }
        csvReader.close();
        table.insertIdColumn();
        table.copyOriginalToActual();
        table.setActualState();
    }

    @Override
    public void exportToFile(String fp, GenericTable table, List<String> columns) throws IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        for(int c = 0, cm = columns.size(); c < cm; c++) {
            if(c > 0) writer.write(separator);
            writer.write("\"" + table.getColumnLabel(columns.get(c)) + "\"");
        }
        writer.newLine();
        
        int ncols = columns.size(), nrows = table.getRowCount();
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++) {
                if(c > 0) writer.write(separator);
                writer.write(Double.toString(table.getValue(r, columns.get(c))));
            }
            writer.newLine();
            IJ.showProgress((double)r / (double)nrows);
        }
        
        writer.close();
    }

}
