package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJPerformanceTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import javax.swing.JSeparator;

public class PerformanceEvaluationPlugIn implements PlugIn {
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        //
        if("showGroundTruthTable".equals(command)) {
            IJGroundTruthTable.getGroundTruthTable().show();
            return;
        }
        if("showPerformanceTable".equals(command)) {
            IJPerformanceTable.getPerformanceTable().show();
            return;
        }
        if(!IJResultsTable.isResultsWindow() || !IJGroundTruthTable.isGroundTruthWindow()) {
            IJ.error("Requires `" + IJResultsTable.IDENTIFIER + "` and `" + IJGroundTruthTable.IDENTIFIER + "` windows open!");
        }
        //
        try {
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Performance evaluation");
            gd.addNumericField("Linear drift distance [px]: ", 0, 0);
            gd.addNumericField("Linear drift angle [deg]: ", 0, 0);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addFileField("Grayscale mask (optional): ", "");
            gd.showDialog();
            
            if(!gd.wasCanceled()) {
                readParams(gd);
                runEvaluation();
            }
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
    }
    
    private void readParams(GenericDialogPlus gd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void runEvaluation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
