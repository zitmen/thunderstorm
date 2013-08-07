package cz.cuni.lf1.lge.ThunderSTORM;

import ij.IJ;
import ij.Menus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

/**
 * This sub-plugin was developed based on the code of Updater_Plugin_.jar
 * available in ImageJ's plugin repository.
 */
public class UpdaterPlugIn implements PlugIn {

    @Override
    public void run(String arg) {
        File file = new File(Menus.getPlugInsPath() + "/" + ThunderSTORM.FILE_NAME + ".jar");
        if (!file.exists()) {
            error("File not found: " + file.getPath());
            return;
        }
        if (!file.canWrite()) {
            String msg = "No write access: " + file.getPath();
            if (IJ.isVista()) {
                msg += Prefs.vistaHint;
            }
            error(msg);
            return;
        }
        String[] list = openUrlAsList(ThunderSTORM.URL + "/list.txt");
        int count = list.length;
        String[] versions = new String[count];
        String[] urls = new String[count];
        for (int i = 0; i < count; i++) {
            versions[i] = list[i];
            urls[i] = ThunderSTORM.URL + "/" + ThunderSTORM.FILE_NAME + "-" + versions[i] + ".jar";
        }
        int choice = showDialog(versions);
        if (choice == -1) {
            return;
        }
        saveJar(file, getJar(urls[choice]));
        updateMenus();
    }

    int showDialog(String[] versions) {
        GenericDialog gd = new GenericDialog("ThunderSTORM Updater");
        gd.addChoice("Upgrade To:", versions, versions[0]);
        String msg =
                "You are currently running v" + version() + ".\n"
                + " \n"
                + "If you click \"OK\", ImageJ will reload ThunderSTORM.\n";
        gd.addMessage(msg);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return -1;
        } else {
            return gd.getNextChoiceIndex();
        }
    }

    byte[] getJar(String address) {
        byte[] data;
        try {
            URL url = new URL(address);
            URLConnection uc = url.openConnection();
            int len = uc.getContentLength();
            IJ.showStatus("Downloading Thunder_STORM.jar (" + IJ.d2s((double) len / 1048576, 1) + "MB)");
            InputStream in = uc.getInputStream();
            data = new byte[len];
            int n = 0;
            while (n < len) {
                IJ.showProgress((double)n / (double)len);
                int count = in.read(data, n, len - n);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            }
            in.close();
            IJ.showStatus("Done.");
            IJ.showProgress(1.0);
        } catch (IOException e) {
            IJ.showStatus("Download failed.");
            return null;
        }
        return data;
    }

    void saveJar(File f, byte[] data) {
        try {
            FileOutputStream out = new FileOutputStream(f);
            out.write(data, 0, data.length);
            out.close();
        } catch (IOException e) {
        }
    }

    String[] openUrlAsList(String address) {
        Vector v = new Vector();
        try {
            URL url = new URL(address);
            InputStream in = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (!line.equals("")) {
                    v.addElement(line);
                }
            }
            br.close();
        } catch (Exception e) {
        }
        String[] lines = new String[v.size()];
        v.copyInto((String[]) lines);
        return lines;
    }

    String version() {
        return ThunderSTORM.VERSION;
    }

    boolean isMac() {
        String osname = System.getProperty("os.name");
        return osname.startsWith("Mac");
    }

    void error(String msg) {
        IJ.error("ImageJ Updater", msg);
    }

    void updateMenus() {
        if (IJ.debugMode) {
            long start = System.currentTimeMillis();
            Menus.updateImageJMenus();
            IJ.log("Update Menus: " + (System.currentTimeMillis() - start) + " ms");
        } else {
            Menus.updateImageJMenus();
        }
    }
}
