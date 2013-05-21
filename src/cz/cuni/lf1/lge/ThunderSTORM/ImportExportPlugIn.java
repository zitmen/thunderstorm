package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.IImportExport;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Vector;

public class ImportExportPlugIn implements PlugIn, ItemListener {

    private String [] modules = null;
    private Vector<IImportExport> ie = null;
    private int active_ie = 0;
    
    @Override
    public void run(String command) {
        try {
            ie = ModuleLoader.getModules(IImportExport.class);
            
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus(command);
            
            modules = new String[ie.size()];
            for(int i = 0; i < modules.length; i++) {
                modules[i] = ie.elementAt(i).getName();
            }
            gd.addChoice("File type", modules, modules[active_ie]);
            ((Choice)gd.getChoices().get(0)).addItemListener(this);
            gd.addFileField("Choose a file", System.getProperty("user.home") + "\\results.txt");
            gd.showDialog();
            
            if(!gd.wasCanceled()) {
                if("export".equals(command)) {
                    exportToFile(gd.getNextString());
                } else {
                    importFromFile(gd.getNextString());
                }
            }
        } catch (Exception ex) {
            IJ.log(ex.getMessage());
        }
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {
        String selected = (String) e.getItem();
        for(int i = 0; i < modules.length; i++) {
            if(selected.equals(modules[i])) {
                active_ie = i;
                break;
            }
        }
    }

    private void exportToFile(String fpath) throws IOException {
        ResultsTable rt = Analyzer.getResultsTable();
        if (rt == null || !IJ.isResultsWindow()) {
            IJ.error("Requires Results window open!");
            return;
        }
        
        IJ.showStatus("ThunderSTORM is exporting your results...");
        IJ.showProgress(0.0);            
        IImportExport exporter = ie.elementAt(active_ie);
        exporter.exportToFile(fpath, rt);
        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM has exported your results.");
    }
    
    private void importFromFile(String fpath) throws IOException {
        ResultsTable rt = Analyzer.getResultsTable();
        if (rt == null || !IJ.isResultsWindow()) {
            rt = new ResultsTable();
            Analyzer.setResultsTable(rt);
        }
        
        IJ.showStatus("ThunderSTORM is importing your file...");
        IJ.showProgress(0.0);
        rt.reset();
        IImportExport importer = ie.elementAt(active_ie);
        importer.importFromFile(fpath, rt);
        rt.show("Results");
        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM has imported your file.");
    }

}
