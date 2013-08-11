package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.IImportExport;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
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

    private String [] modules = null;
    private String [] suffix = null;
    private Vector<IImportExport> ie = null;
    private int active_ie = 0;
    private Choice ftype;
    private TextField fpath;
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        //
        try {
            ie = ModuleLoader.getModules(IImportExport.class);
            
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus(command);
            
            modules = new String[ie.size()];
            suffix = new String[ie.size()];
            for(int i = 0; i < modules.length; i++) {
                modules[i] = ie.elementAt(i).getName();
                suffix[i] = ie.elementAt(i).getSuffix();
            }
            gd.addChoice("File type", modules, modules[active_ie]);
            ftype = (Choice)gd.getChoices().get(0);
            ftype.addItemListener(this);
            gd.addFileField("Choose a file", IJ.getDirectory("current") + "results." + suffix[active_ie]);
            fpath = (TextField)gd.getStringFields().get(0);
            fpath.addTextListener(this);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            //
            String [] col_headers = null;
            if("export".equals(command)) {
                gd.addMessage("Columns to export:");
                IJResultsTable rt = IJResultsTable.getResultsTable();
                col_headers = rt.getColumnNames();
                boolean [] active_columns = new boolean[col_headers.length];
                Arrays.fill(active_columns, true); active_columns[rt.findColumn(PSFInstance.LABEL_ID)] = false;
                gd.addCheckboxGroup(col_headers.length, 1, col_headers, active_columns);
            } else if("import".equals(command)) {
                gd.addCheckbox("clear the table of results before import", true);
            }
            gd.showDialog();
            
            if(!gd.wasCanceled()) {
                active_ie = gd.getNextChoiceIndex();
                String filePath = gd.getNextString();
                if("export".equals(command)) {
                    Vector<String> columns = new Vector<String>();
                    for(int i = 0; i < col_headers.length; i++) {
                        if(gd.getNextBoolean() == true) {
                            columns.add(col_headers[i]);
                        }
                    }
                    exportToFile(filePath, columns);
                } else if("import".equals(command)) {
                    importFromFile(filePath, gd.getNextBoolean());
                }
            }
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
    }
    
    private void exportToFile(String fpath, Vector<String> columns) {
        IJ.showStatus("ThunderSTORM is exporting your results...");
        IJ.showProgress(0.0);
        try {
            IImportExport exporter = ie.elementAt(active_ie);
            exporter.exportToFile(fpath, IJResultsTable.getResultsTable(), columns);
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
    
    private void importFromFile(String fpath, boolean reset_first) {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        IJ.showStatus("ThunderSTORM is importing your file...");
        IJ.showProgress(0.0);
        try {
            if(reset_first) rt.reset();
            rt.setOriginalState();
            IImportExport importer = ie.elementAt(active_ie);
            importer.importFromFile(fpath, rt);
            IJ.showStatus("ThunderSTORM has imported your file.");
        } catch(IOException ex) {
            IJ.showStatus("");
            IJ.showMessage("Exception", ex.getMessage());
        } catch(Exception ex) {
            IJ.showStatus("");
            IJ.handleException(ex);
        }
        rt.show("Results");
        IJ.showProgress(1.0);
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
        if(fname.isEmpty()) return;
        int dotpos = fname.lastIndexOf('.');
        if(dotpos < 0) return;
        String type = fname.substring(dotpos + 1).trim();
        for(int i = 0; i < suffix.length; i++) {
            if(type.equals(suffix[i])) {
                ftype.select(i);
                break;
            }
        }
    }

}
