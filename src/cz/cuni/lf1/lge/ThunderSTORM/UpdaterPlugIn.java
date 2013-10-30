package cz.cuni.lf1.lge.ThunderSTORM;

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.Menus;
import ij.Prefs;
import ij.plugin.PlugIn;
import java.awt.Color;
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
import javax.swing.JLabel;

public class UpdaterPlugIn implements PlugIn {

    @Override
    public void run(String arg) {
        IJ.showStatus("Checking the file access rights...");
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
        IJ.showStatus("Looking for new versions...");
        String[] list = openUrlAsList(ThunderSTORM.URL_DAILY + "/list.txt");
        int count = list.length;
        String[] versions = new String[count];
        String[] urls = new String[count];
        for (int i = 0; i < count; i++) {
            versions[i] = list[i];
            urls[i] = ThunderSTORM.URL_DAILY + "/" + versions[i] + ".jar";
        }
        IJ.showStatus("");
        int choice = showDialog(versions);
        if (choice == -1) {
            return;
        }
        saveJar(file, getJar(urls[choice]));
        ModuleLoader.setUseCaching(false);
        IJ.showMessage("Updater", "Please restart ImageJ to complete ThunderSTORM update.");
        updateMenus();
    }

    int showDialog(String[] versions) {
        String newerAvailable = null;
        Version current = new Version(version());
        for(String ver : versions) {
            if(current.compareTo(new Version(ver)) < 0) {
                newerAvailable = ver;
                break;
            }
        }
        //
        GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM Updater");
        if(newerAvailable == null) {
            JLabel label = new JLabel("ThunderSTORM is up to date!");
            label.setForeground(new Color(0, 128, 0));
            gd.addComponent(label);
        } else {
            JLabel label = new JLabel("New version of ThunderSTORM is available!");
            label.setForeground(new Color(128, 0, 0));
            gd.addComponent(label);
        }
        gd.addChoice("Available versions:", versions, newerAvailable);
        String msg =
                "You are currently running version " + version() + ".\n"
                + " \n"
                + "If you click \"OK\", ImageJ will download the selected\n"
                + "version and reload the ThunderSTORM.\n";
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
            IJ.showStatus("Downloading Thunder_STORM.jar ...");
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
            IJ.handleException(e);
            return null;
        }
        return data;
    }

    void saveJar(File f, byte[] data) {
        try {
            IJ.showStatus("Installing Thunder_STORM.jar ...");
            FileOutputStream out = new FileOutputStream(f);
            out.write(data, 0, data.length);
            out.close();
            IJ.showStatus("Done.");
        } catch (IOException e) {
            IJ.showStatus("Update failed.");
            IJ.handleException(e);
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
        } catch (Exception ex) {
            IJ.showMessage("Error!", "Connection problem! Check you connection to the Internet or your firewall settings.");
            IJ.handleException(ex);
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
    
    class Version implements Comparable<Version> {
        
        public String version;
        public String lastStable;
        public int year;
        public int month;
        public int day;
        public int buildOfTheDay;
        
        public Version(String ver) {
            this.version = ver;
            String [] tokens = ver.split("-");
            this.lastStable = tokens[0];
            this.year = Integer.parseInt(tokens[1]);
            this.month = Integer.parseInt(tokens[2]);
            this.day = Integer.parseInt(tokens[3]);
            this.buildOfTheDay = Integer.parseInt(tokens[4].substring(1));
        }
        
        @Override
        public int compareTo(Version v) {
            int cmp = year - v.year;
            if(cmp != 0) return cmp;
            cmp = month - v.month;
            if(cmp != 0) return cmp;
            cmp = day - v.day;
            if(cmp != 0) return cmp;
            return (buildOfTheDay - v.buildOfTheDay);
        }
    }
}
