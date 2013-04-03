package ThunderSTORM.utils;

import au.com.bytecode.opencsv.CSVReader;
import com.sun.media.sound.InvalidDataException;
import ij.process.FloatProcessor;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CSV {
    
    public static FloatProcessor csv2fp(String fname) throws IOException {
        
        CSVReader csvReader = new CSVReader(new FileReader(fname));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        
        float [][] array = new float[lines.get(0).length][lines.size()];
        for(int c = 0; c < array.length; c++) {
            for(int r = 0; r < array[c].length; r++) {
                array[c][r] = Float.parseFloat(lines.get(r)[c]);
            }
        }
        return new FloatProcessor(array);
    }
}
