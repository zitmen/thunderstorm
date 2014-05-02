package cz.cuni.lf1.lge.ThunderSTORM.UI;

import ij.IJ;
import java.net.URL;
import javax.swing.JButton;

public class Help {

    public static JButton createHelpButton(String name) {
        String resource = getResourcePath(name);
        URL url = getUrl(resource);
        if(url == null) {
            IJ.log("Could not load help file: " + resource);
        }
        return new HelpButton(url);
    }

    public static JButton createHelpButton(Class clazz) {
        return createHelpButton(clazz.getName());
    }

    public static boolean existsHelpForClass(Class clazz) {
        return existsHelpForName(clazz.getName());
    }

    public static boolean existsHelpForName(String name) {
        return getUrl(getResourcePath(name)) != null;
    }

    public static String getResourcePath(String name) {
        return "resources/help/" + name.replace('.', '/') + ".html";
    }

    public static URL getUrl(String path) {
        return IJ.getClassLoader().getResource(path);
    }
}
