package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import java.util.List;

public class TableHandlerPlugin implements PlugIn {

    @Override
    public void run(String arg) {
        GUI.setLookAndFeel();
        CameraSetupPlugIn.loadPreferences();
        //
        //
        String options = Macro.getOptions();
        String action = options == null ? "show" : Macro.getValue(options, "action", "show");
        try {
            IJResultsTable resultsTable = IJResultsTable.getResultsTable();
            if("show".equals(action)) {
                resultsTable.show();
            } else if("reset".equals(action)) {
                resultsTable.copyOriginalToActual();
                resultsTable.getOperationHistoryPanel().removeAllOperations();
                resultsTable.setStatus("Results reset.");
                resultsTable.showPreview();
            } else if("undo-redo".equals(action)) {
                resultsTable.getOperationHistoryPanel().undoOrRedoLastOperation();

            } else {
                List<? extends PostProcessingModule> modules = resultsTable.getPostProcessingModules();

                PostProcessingModule selectedModule = null;
                for(PostProcessingModule module : modules) {
                    if(module.getMacroName().equals(action)) {
                        selectedModule = module;
                        break;
                    }
                }
                if(selectedModule != null) {
                    selectedModule.run();
                } else {
                    throw new IllegalArgumentException("Post processing module not found for action: " + action);
                }
            }

        } catch(Exception e) {
            IJ.handleException(e);
        }
    }

    public static void recordReset() {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "reset");
            Recorder.saveCommand();
        }
    }

    public static void recordUndoOrRedo() {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "undo-redo");
            Recorder.saveCommand();
        }
    }
}
