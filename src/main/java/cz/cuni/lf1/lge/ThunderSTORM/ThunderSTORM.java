package cz.cuni.lf1.lge.ThunderSTORM;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ThunderSTORM {

    private static Properties props = loadProperties();
    public static final String URL_DAILY = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/daily";
    public static final String URL_STABLE = "https://googledrive.com/host/0BzOGc-xMFyDYR1JaelZYQmJsaUE/builds/stable";
    public static final String FILE_NAME = "Thunder_STORM";

    public static String getVersion() {
        return props.getProperty("version");
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try {
            InputStream stream = ThunderSTORM.class.getClassLoader().getResourceAsStream("thunderstorm.properties");
            properties.load(stream);
            stream.close();
        } catch (IOException ex) {
            properties.setProperty("version", "dev-0000-00-00-b1");
        }
        return properties;
    }
}
