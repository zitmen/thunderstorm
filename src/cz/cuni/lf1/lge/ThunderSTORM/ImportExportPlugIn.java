package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.IImportExport;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.Prefs;
import ij.WindowManager;
import ij.plugin.PlugIn;
import java.awt.Choice;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JSeparator;

public class ImportExportPlugIn implements PlugIn, ItemListener, TextListener {

    public static final String IMPORT = "import";
    public static final String EXPORT = "export";
    private String[] modules = null;
    private String[] suffix = null;
    private Vector<IImportExport> ie = null;
    private Choice ftype;
    private TextField fpath;
    
    private int active_ie = 0;
    private int startingFrame = 1;
    private String path = "";
    private boolean resetFirst = true;
    private boolean livePreview = true;
    private boolean saveMeasurementProtocol = true;

    public ImportExportPlugIn() {
        super();
        this.path = null;
    }

    public ImportExportPlugIn(String path) {
        super();
        this.path = path;
    }

    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        loadPreferences();
        //
        String[] commands = command.split(";");
        if(commands.length != 2) {
            throw new IllegalArgumentException("Malformatted argument for Import/Export plug-in!");
        }
        //
        try {
            ie = ModuleLoader.getModules(IImportExport.class);

            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus(commands[0] + " " + commands[1]);

            modules = new String[ie.size()];
            suffix = new String[ie.size()];
            for(int i = 0; i < modules.length; i++) {
                modules[i] = ie.elementAt(i).getName();
                suffix[i] = ie.elementAt(i).getSuffix();
            }
            gd.addChoice("File type", modules, modules[active_ie]);
            ftype = (Choice) gd.getChoices().get(0);
            ftype.addItemListener(this);
            if((path == null) || path.isEmpty()) {
                gd.addFileField("Choose a file", IJ.getDirectory("current") + commands[1] + "." + suffix[active_ie]);
            } else {
                gd.addFileField("Choose a file", path);
            }
            fpath = (TextField) gd.getStringFields().get(0);
            fpath.addTextListener(this);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            //
            String[] col_headers = null;
            if(EXPORT.equals(commands[0])) {
                col_headers = fillExportPane(commands[1], gd);
            } else if(IMPORT.equals(commands[0])) {
                fillImportPane(commands[1], gd);
            }
            gd.add(Help.createHelpButton("help"));
            gd.showDialog();

            if(!gd.wasCanceled()) {
                active_ie = gd.getNextChoiceIndex();
                String filePath = gd.getNextString();
                if(EXPORT.equals(commands[0])) {
                    runExport(commands[1], gd, filePath, col_headers);
                } else if(IMPORT.equals(commands[0])) {
                    runImport(commands[1], gd, filePath);
                }
                savePreferences();
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        String fp = fpath.getText();
        if(fp.endsWith("\\") || fp.endsWith("/")) {
            fpath.setText(fp + "results." + suffix[ftype.getSelectedIndex()]);
        } else {
            int dotpos = fp.lastIndexOf('.');
            if(dotpos < 0) {
                fpath.setText(fp + '.' + suffix[ftype.getSelectedIndex()]);
            } else {
                fpath.setText(fp.substring(0, dotpos + 1) + suffix[ftype.getSelectedIndex()]);
            }
        }
    }

    @Override
    public void textValueChanged(TextEvent e) {
        String fname = new File(fpath.getText()).getName().trim();
        if(fname.isEmpty()) {
            return;
        }
        int dotpos = fname.lastIndexOf('.');
        if(dotpos < 0) {
            return;
        }
        String type = fname.substring(dotpos + 1).trim();
        for(int i = 0; i < suffix.length; i++) {
            if(type.equals(suffix[i])) {
                ftype.select(i);
                break;
            }
        }
    }

    private String getProtocolFilePath(String fpath) {
        int dotpos = fpath.lastIndexOf('.');
        if(dotpos < 0) {
            return fpath + "-protocol.txt";
        } else {
            return fpath.substring(0, dotpos) + "-protocol.txt";
        }
    }

    private String[] fillExportPane(String cmd, GenericDialogPlus gd) {
        String[] col_headers;
        GenericTable table;
        if(IJGroundTruthTable.IDENTIFIER.equals(cmd)) {
            table = IJGroundTruthTable.getGroundTruthTable();
        } else {
            if(IJResultsTable.getResultsTable().getMeasurementProtocol() != null) {
                gd.addCheckbox("export measurement protocol", saveMeasurementProtocol);
            }
            table = IJResultsTable.getResultsTable();
        }
        //
        gd.addMessage("Columns to export:");
        col_headers = (String[]) table.getColumnNames().toArray(new String[0]);
        boolean[] active_columns = new boolean[col_headers.length];
        Arrays.fill(active_columns, true);
        int colId = table.findColumn(MoleculeDescriptor.LABEL_ID);
        if(colId >= 0) {
            active_columns[colId] = false;
        }
        gd.addCheckboxGroup(col_headers.length, 1, col_headers, active_columns);
        //
        return col_headers;
    }

    private void fillImportPane(String cmd, GenericDialogPlus gd) {
        gd.addNumericField("Starting frame number: ", startingFrame, 0);
        gd.addCheckbox("clear the `" + cmd + "` table before import", resetFirst);
        if(IJResultsTable.IDENTIFIER.equals(cmd)) {
            gd.addCheckbox("show rendering preview", livePreview);
            int[] openedImagesIds = WindowManager.getIDList();
            if(openedImagesIds != null) {
                String[] openedImagesTitles = new String[openedImagesIds.length + 1];
                openedImagesTitles[0] = "";
                for(int i = 0; i < openedImagesIds.length; i++) {
                    openedImagesTitles[i + 1] = WindowManager.getImage(openedImagesIds[i]).getTitle();
                }
                gd.addMessage("If the input image for the imported results is opened, which one is it?\n"
                        + " It can be used for overlay preview of detected molecules.");
                gd.addChoice("The input image: ", openedImagesTitles, "");
            }
        }
    }

    private void runExport(String cmd, GenericDialogPlus gd, String filePath, String[] col_headers) {
        GenericTable table;
        if(IJGroundTruthTable.IDENTIFIER.equals(cmd)) {
            table = IJGroundTruthTable.getGroundTruthTable();
        } else {    // IJResultsTable
            IJResultsTable rt = IJResultsTable.getResultsTable();
            if(rt.getMeasurementProtocol() != null) {
                saveMeasurementProtocol = gd.getNextBoolean();
                if(saveMeasurementProtocol) {
                    rt.getMeasurementProtocol().export(getProtocolFilePath(filePath));
                }
            }
            table = rt;
        }
        //
        Vector<String> columns = new Vector<String>();
        for(int i = 0; i < col_headers.length; i++) {
            if(gd.getNextBoolean() == true) {
                columns.add(col_headers[i]);
            }
        }
        exportToFile(table, filePath, columns);
    }

    private void runImport(String cmd, GenericDialogPlus gd, String filePath) {
        startingFrame = (int) gd.getNextNumber();
        if(IJGroundTruthTable.IDENTIFIER.equals(cmd)) {
            importFromFile(IJGroundTruthTable.getGroundTruthTable(), filePath, gd.getNextBoolean());
        } else {    // IJResultsTable
            IJResultsTable rt = IJResultsTable.getResultsTable();
            resetFirst = gd.getNextBoolean();
            try {
                rt.setAnalyzedImage(WindowManager.getImage(gd.getNextChoice()));
            } catch(ArrayIndexOutOfBoundsException ex) {
                if(resetFirst) {
                    rt.setAnalyzedImage(null);
                }
            }
            importFromFile(rt, filePath, resetFirst);
            livePreview = gd.getNextBoolean();
            rt.setLivePreview(livePreview);
            rt.showPreview();
        }
    }

    private void exportToFile(GenericTable table, String fpath, Vector<String> columns) {
        IJ.showStatus("ThunderSTORM is exporting your results...");
        IJ.showProgress(0.0);
        try {
            IImportExport exporter = ie.elementAt(active_ie);
            exporter.exportToFile(fpath, table, columns);
            IJ.showStatus("ThunderSTORM has exported your results.");
        } catch(IOException ex) {
            IJ.showStatus("");
            IJ.showMessage("Exception", ex.getMessage());
        } catch(Exception ex) {
            IJ.showStatus("");
            IJ.handleException(ex);
        }
        IJ.showProgress(1.0);
    }

    private void importFromFile(GenericTable table, String fpath, boolean reset_first) {
        IJ.showStatus("ThunderSTORM is importing your file...");
        IJ.showProgress(0.0);
        try {
            if(reset_first) {
                table.reset();
            }
            table.setOriginalState();
            IImportExport importer = ie.elementAt(active_ie);
            importer.importFromFile(fpath, table, startingFrame);
            IJ.showStatus("ThunderSTORM has imported your file.");
        } catch(IOException ex) {
            IJ.showStatus("");
            IJ.showMessage("Exception", ex.getMessage());
        } catch(Exception ex) {
            IJ.showStatus("");
            IJ.handleException(ex);
        }
        if(table instanceof IJResultsTable) {
            AnalysisPlugIn.setDefaultColumnsWidth((IJResultsTable) table);
        }
        table.show();
        IJ.showProgress(1.0);
    }
    
    public void loadPreferences() {
        active_ie = Integer.parseInt(Prefs.get("thunderstorm.io.active", "0"));
        startingFrame = Integer.parseInt(Prefs.get("thunderstorm.io.startingFrame", "1"));
        path = Prefs.get("thunderstorm.io.path", "");
        resetFirst = Boolean.parseBoolean(Prefs.get("thunderstorm.io.resetTable", "true"));
        livePreview = Boolean.parseBoolean(Prefs.get("thunderstorm.io.livePreview", "true"));
        saveMeasurementProtocol = Boolean.parseBoolean(Prefs.get("thunderstorm.io.saveMeasurementProtocol", "true"));
    }
    
    public void savePreferences() {
        Prefs.set("thunderstorm.io.active", Integer.toString(active_ie));
        Prefs.set("thunderstorm.io.startingFrame", Integer.toString(startingFrame));
        Prefs.set("thunderstorm.io.path", path);
        Prefs.set("thunderstorm.io.resetTable", Boolean.toString(resetFirst));
        Prefs.set("thunderstorm.io.livePreview", Boolean.toString(livePreview));
        Prefs.set("thunderstorm.io.saveMeasurementProtocol", Boolean.toString(saveMeasurementProtocol));
    }

}
