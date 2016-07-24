package cz.cuni.lf1.lge.ThunderSTORM;

import ij.IJ;

import java.io.IOException;
import java.util.Properties;

public class ThunderSTORM {
    
    public static final String URL_DAILY = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/daily";
    public static final String URL_STABLE = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable";
    public static final String FILE_NAME = "Thunder_STORM";

    public static final String VERSION;
    static {
        try {
            Properties p = new Properties();
            p.load(IJ.getClassLoader().getResourceAsStream("thunderstorm.properties"));
            VERSION = p.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Can't read build properties! The package is probably corrupted.");
        }
    }

}
