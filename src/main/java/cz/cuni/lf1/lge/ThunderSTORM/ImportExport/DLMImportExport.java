package cz.cuni.lf1.lge.ThunderSTORM.ImportExport;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import ij.IJ;
import org.apache.commons.io.input.CountingInputStream;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

abstract public class DLMImportExport implements IImportExport {
    
    private String separator;
    
    public DLMImportExport(String separator) {
        this.separator = separator;
    }

    @Override
    public void importFromFile(String fp, GenericTable table, int startingFrame) throws IOException {
        assert (table != null);
        assert (fp != null);
        assert (!fp.isEmpty());
        File file = new File(fp);
        long fileSize = file.length();
        //im using file size and counting bytes read to track progress (without having to know line count in advance)
        Scanner sc = null;
        CountingInputStream countingInputStream = null;
        try {
            countingInputStream = new CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
            sc = new Scanner(countingInputStream, "UTF-8");

            if (!sc.hasNextLine()) return;
            String[] firstLine = splitLine(sc.nextLine());
            if (firstLine.length < 2) return;

            Vector<Pair<String, Units>> cols = new Vector<Pair<String, Units>>();
            int c_id = -1;
            for (int c = 0, cm = firstLine.length; c < cm; c++) {
                Pair<String, Units> tmp = GenericTable.parseColumnLabel(firstLine[c]);
                if (MoleculeDescriptor.LABEL_ID.equals(tmp.first)) {
                    c_id = c;
                    continue;
                }
                cols.add(tmp);
            }
            String[] colnames = new String[cols.size()];
            Units[] colunits = new Units[cols.size()];
            for (int c = 0, cm = cols.size(); c < cm; c++) {
                colnames[c] = cols.elementAt(c).first;
                colunits[c] = cols.elementAt(c).second;
            }
            //
            if (!table.columnNamesEqual(colnames)) {
                throw new IOException("Labels in the file do not correspond to the header of the table (excluding '" + MoleculeDescriptor.LABEL_ID + "')!");
            }
            if (table.isEmpty()) {
                table.setDescriptor(new MoleculeDescriptor(colnames, colunits));
            }
            //
            double[] values = new double[colnames.length];
            String[] line;
            int dataColCount = values.length + (c_id < 0 ? 0 : 1);
            while (sc.hasNextLine()) {
                line = splitLine(sc.nextLine());
                if (line.length == 1 && line[0].isEmpty()) continue; // just an empty line...do not report
                if (line.length != dataColCount) { // but report when there is a corrupted row
                    IJ.log("A line has different number of items than the header! Skipping over...");
                    continue;
                }
                for(int c = 0, ci = 0, cm = line.length; c < cm; c++) {
                    if(c == c_id) continue;
                    values[ci] = Double.parseDouble(line[c]);
                    if(MoleculeDescriptor.LABEL_FRAME.equals(colnames[ci])) {
                        values[ci] += startingFrame - 1;
                    }
                    ci++;
                }
                table.addRow(values);

                IJ.showProgress((double) countingInputStream.getByteCount() / (double)fileSize);
            }
        } finally {
            if (countingInputStream != null) countingInputStream.close();
            if (sc != null) sc.close();
        }
        table.insertIdColumn();
        table.copyOriginalToActual();
        table.setActualState();
    }

    private String[] splitLine(String line) {
        String[] arr = line.split(separator);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
            if (arr[i].startsWith("\"") && arr[i].endsWith("\"") ||
                arr[i].startsWith("'") && arr[i].endsWith("'") ||
                arr[i].startsWith("`") && arr[i].endsWith("`")) {
                arr[i] = arr[i].substring(1, arr[i].length() - 1);
            }
        }
        return arr;
    }

    @Override
    public void exportToFile(String fp, int floatPrecision, GenericTable table, List<String> columns) throws IOException {
        assert(table != null);
        assert(fp != null);
        assert(!fp.isEmpty());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(fp));
        for(int c = 0, cm = columns.size(); c < cm; c++) {
            if(c > 0) writer.write(separator);
            writer.write("\"" + table.getColumnLabel(columns.get(c)) + "\"");
        }
        writer.newLine();

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        df.setMaximumFractionDigits(floatPrecision);

        int ncols = columns.size(), nrows = table.getRowCount();
        for(int r = 0; r < nrows; r++) {
            for(int c = 0; c < ncols; c++) {
                if(c > 0) writer.write(separator);
                writer.write(df.format(table.getValue(r, columns.get(c))));
            }
            writer.newLine();
            IJ.showProgress((double)r / (double)nrows);
        }
        
        writer.close();
    }

}
