package cz.cuni.lf1.lge.ThunderSTORM.UI;

import ij.IJ;
import java.net.URL;
import javax.swing.JButton;

public class Help {

    
    public static JButton createHelpButton(String name) {
        return new HelpButton(getUrl(name));
    }

    public static JButton createHelpButton(Class clazz) {
        return new HelpButton(getUrl(clazz.getName()));
    }

    public static boolean existsHelpForClass(Class clazz) {
        return existsHelpForName(clazz.getName());
    }

    public static boolean existsHelpForName(String name) {
        return getUrl(name) != null;
    }

    public static URL getUrl(String name) {
        return IJ.getClassLoader().getResource("resources/help/" + name + ".html");
    }
}
