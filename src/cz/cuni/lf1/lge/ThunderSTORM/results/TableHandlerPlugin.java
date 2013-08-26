package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;

public class TableHandlerPlugin implements PlugIn {

    @Override
    public void run(String arg) {
        String options = Macro.getOptions();
        String action = options == null ? "show" : Macro.getValue(options, "action", "show");
        try {
            IJResultsTable resultsTable = IJResultsTable.getResultsTable();
            if("show".equals(action)) {
                GUI.setLookAndFeel();
                resultsTable.show();
            } else if("drift".equals(action)) {
                int steps = Integer.parseInt(Macro.getValue(options, "steps", "5"));
                double magnification = Double.parseDouble(Macro.getValue(options, "magnification", "5"));
                boolean showCorrelations = Boolean.parseBoolean(Macro.getValue(options, "showCorrelations", "true"));
                boolean showDrift = Boolean.parseBoolean(Macro.getValue(options, "showDrift", "true"));

                resultsTable.getDriftCorrection().runDriftCorrection(steps, magnification, showCorrelations, showDrift);
            } else if("merge".equals(action)) {
                double dist = Double.parseDouble(Macro.getValue(options, "dist", "0"));

                resultsTable.getGrouping().runGrouping(dist);
            } else if("filter".equals(action)) {
                String formula = Macro.getValue(options, "formula", "");

                resultsTable.getFilter().runFilter(formula);
            } else if("reset".equals(action)) {
                resultsTable.copyOriginalToActual();
                resultsTable.getOperationHistoryPanel().removeAllOperations();
                resultsTable.setStatus("Results reset.");
                resultsTable.showPreview();
            } else if("undo-redo".equals(action)) {
                resultsTable.getOperationHistoryPanel().undoOrRedoLastOperation();
            } else if("offset".equals(action)){
                int framesPerStagePosition = Integer.parseInt(Macro.getValue(options, "framesPerStagePos", "0"));
                int stagePositions = Integer.parseInt(Macro.getValue(options, "stagePositions", "0"));
                double stageStep = Double.parseDouble(Macro.getValue(options, "stageStep", "0"));
                double firstPositionOffset = Double.parseDouble(Macro.getValue(options, "firstPosOffset", "0"));
                
                resultsTable.getStageOffset().runAddStageOffset(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset);
            }
            
        } catch(Exception e) {
            IJ.handleException(e);
        }
    }

    public static void recordFilter(String formula) {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "filter");
            Recorder.recordOption("formula", formula);
            Recorder.saveCommand();
        }
    }

    public static void recordDrift(int steps, double magnification, boolean showCorrelations, boolean showDrift) {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "drift");
            Recorder.recordOption("steps", steps + "");
            Recorder.recordOption("magnification", magnification + "");
            Recorder.recordOption("showCorrelations", showCorrelations + "");
            Recorder.recordOption("showDrift", showDrift + "");
            Recorder.saveCommand();
        }
    }

    public static void recordMerging(double dist) {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "merge");
            Recorder.recordOption("dist", dist + "");
            Recorder.saveCommand();
        }
    }

    public static void recordStageOffset(int framesPerStagePosition, int stagePositions, double stageStep, double firstPositionOffset) {
        if(Recorder.record) {
            Recorder.setCommand("Show results table");
            Recorder.recordOption("action", "offset");
            Recorder.recordOption("framesPerStagePos", framesPerStagePosition + "");
            Recorder.recordOption("stagePositions", stagePositions + "");
            Recorder.recordOption("stageStep", stageStep + "");
            Recorder.recordOption("firstPosOffset", firstPositionOffset + "");
            Recorder.saveCommand();
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
