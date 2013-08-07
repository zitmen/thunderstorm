package cz.cuni.lf1.lge.ThunderSTORM;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.URL;

public class AboutPlugIn implements PlugIn {

    public static final String FILE_NAME = "about.png";
    
    @Override
    public void run(String arg) {
        ImageProcessor ip = null;
        ImageJ ij = IJ.getInstance();
        URL url = ij.getClass().getResource("/" + FILE_NAME);
        if (url != null) {
            Image img = null;
            try {
                img = ij.createImage((ImageProducer)url.getContent());
            } catch (Exception e) {
            }
            if (img != null) {
                ImagePlus imp = new ImagePlus("About ThunderSTORM", img);
                ip = imp.getProcessor();
                imp.show();
            }
        }
    }
}
