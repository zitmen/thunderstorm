package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import ij.IJ;
import ij.Menus;
import ij.Prefs;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class UpdaterPlugIn implements PlugIn {

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
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
        String[] dailyList = openUrlAsList(ThunderSTORM.URL_DAILY + "/list.txt");
        String[] stableList = openUrlAsList(ThunderSTORM.URL_STABLE + "/list.txt");
        List<Version> allVersions = new ArrayList<Version>(dailyList.length+ stableList.length);
        for(String filename :dailyList){
            allVersions.add(new Version(filename));
        }
        for(String filename :stableList){
            allVersions.add(new Version(filename));
        }
        Collections.sort(allVersions, Collections.reverseOrder());
        
        int choice = showDialog(allVersions);
        if (choice == -1) {
            return;
        }
        saveJar(file, getJar(allVersions.get(choice).getUrl()));
        IJ.showMessage("Updater", "Please restart ImageJ to complete ThunderSTORM update.");
        ModuleLoader.setUseCaching(false);
        updateMenus();
    }

    int showDialog(List<Version> versions) {
        UpdaterDialog dialog = new UpdaterDialog(versions);
        if(!MacroParser.isRanFromMacro()) {
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return -1;
            }
        }
        return dialog.getChoiceIndex();
    }

    byte[] getJar(String address) {
        byte[] data;
        try {
            IJ.showStatus("Downloading Thunder_STORM.jar ...");
            URL url = new URL(address);
            URLConnection uc = url.openConnection();
            InputStream in = uc.getInputStream();
            int n = 0, len = 1024*1024;    // 1 MiB
            data = new byte[len];
            while (true) {
                IJ.showStatus("Downloading Thunder_STORM.jar (" + IJ.d2s((double) n / 1024 / 1024, 1) + "MB)");
                int count = in.read(data, n, len - n);
                if (count < 0) {
                    break;
                }
                n += count;
                if ((len - n) <= 0) {
                    byte[] tmp = data;
                    len *= 2;
                    data = new byte[len];
                    System.arraycopy(tmp, 0, data, 0, tmp.length);
                }
            }
            in.close();
            //
            byte[] tmp = data;
            data = new byte[n];
            System.arraycopy(tmp, 0, data, 0, n);
            //
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
    
    private Version getNewestStable(List<Version> versions){
        
        for(Version version : versions) {
            if(version.isStable()){
                return version;
            }
        }
        return null;
    }
    
    private Version getNewestDev(List<Version> versions){
        for(Version version : versions) {
            if(!version.isStable()){
                return version;
            }
        }
        return null;
    }
    
    class Version implements Comparable<Version> {
        
        public String fileName;
        public String version;
        public int year;
        public int month;
        public int day;
        public int buildOfTheDay;
        
        
        public Version(String ver) {
            this.fileName = ver;
            String [] tokens = ver.split("-");
            this.version = tokens[0];
            this.year = Integer.parseInt(tokens[1]);
            this.month = Integer.parseInt(tokens[2]);
            this.day = Integer.parseInt(tokens[3]);
            if(tokens.length > 4) {
                this.buildOfTheDay = Integer.parseInt(tokens[4].substring(1));
            }
        }

        @Override
        public String toString() {
            if("dev".equals(version)){
                return fileName;
            }else{
                return version + " (" + year+ "-" + month + "-" + day + ")";
            }
        }
        boolean isStable(){
            return !"dev".equals(version);
        }
        
        public String getUrl(){
            return (isStable()? ThunderSTORM.URL_STABLE : ThunderSTORM.URL_DAILY) + "/" + fileName + ".jar";
        }
        
        @Override
        public int compareTo(Version v) {
            int cmp = year - v.year;
            if(cmp != 0) return cmp;
            cmp = month - v.month;
            if(cmp != 0) return cmp;
            cmp = day - v.day;
            if(cmp != 0) return cmp;
            cmp =  (buildOfTheDay - v.buildOfTheDay);
            if(cmp != 0) return cmp;
            if(isStable() && !v.isStable())
                return 1;
            if(!isStable() && v.isStable())
                return -1;
            if(isStable() && v.isStable()){
                String[] tokens1 = version.split("\\.");
                String[] tokens2 = v.version.split("\\.");
                for(int i = 0; i < tokens1.length || i < tokens2.length; i++){
                    int num1 = Integer.parseInt(tokens1[i]);
                    int num2 = Integer.parseInt(tokens2[i]);
                    cmp = Integer.valueOf(num1).compareTo(num2);
                    if(cmp != 0){
                        return cmp;
                    }
                }
            }
            return 0;
        }
    }
    
    //---------------GUI-----------------------
    class UpdaterDialog extends DialogStub {

        private List<Version> versions;
        private JComboBox versionsComboBox;
        
        public UpdaterDialog(List<Version> versions) {
            super(new ParameterTracker("thunderstorm.updater"), IJ.getInstance(), "ThunderSTORM Updater");
            this.versions = versions;
        }
        
        public int getChoiceIndex() {
            return versionsComboBox.getSelectedIndex();
        }

        @Override
        protected void layoutComponents() {
            Version newestVersion;
            Version current = new Version(version());
            boolean upToDate;
            if(current.isStable()){
                newestVersion = getNewestStable(versions);
                upToDate = !(current.compareTo(newestVersion) < 0);
            }else{
                newestVersion = getNewestDev(versions);
                upToDate = !(current.compareTo(newestVersion) < 0);
            }
            //
            String branch = current.isStable() ? "stable" : "development";
            if(upToDate) {
                JLabel label = new JLabel("ThunderSTORM is up to date! (" + branch + ")");
                label.setForeground(new Color(0, 128, 0));
                add(label, GridBagHelper.twoCols());
            } else {
                JLabel label = new JLabel("New " + branch + " version of ThunderSTORM is available!");
                label.setForeground(new Color(128, 0, 0));
                add(label, GridBagHelper.twoCols());
            }
            //
            String[] versionStrings = new String[versions.size()];
            for(int i = 0; i < versions.size(); i ++){
                versionStrings[i] = versions.get(i).toString();
            }
            //
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            add(new JLabel("Available versions:"), GridBagHelper.leftCol());
            versionsComboBox = new JComboBox(versionStrings);
            versionsComboBox.setSelectedItem(newestVersion.toString());
            add(versionsComboBox, GridBagHelper.rightCol());
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            String msg =  "<html>"
                        + "You are currently running version " + current.toString() + ".<br><br>"
                        + "If you click \"OK\", ImageJ will download the selected<br>"
                        + "version and reload ThunderSTORM.<br>"
                        + "</html>";
            add(new JLabel(msg), GridBagHelper.twoCols());
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            //
            JPanel buttons = new JPanel(new GridBagLayout());
            GridBagConstraints glueConstraints = new GridBagConstraints();
            glueConstraints.fill = GridBagConstraints.HORIZONTAL;
            glueConstraints.weightx = 1;
            buttons.add(Box.createHorizontalGlue(), glueConstraints);
            buttons.add(createOKButton());
            buttons.add(createCancelButton());
            add(buttons, GridBagHelper.twoCols());
            //
            params.loadPrefs();
            getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLocationRelativeTo(null);
            setModal(true);
        }
    }
}
