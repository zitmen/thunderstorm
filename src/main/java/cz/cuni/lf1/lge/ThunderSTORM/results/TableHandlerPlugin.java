
package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.util.List;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;

public class TableHandlerPlugin implements PlugIn {

	private static final String TARGET_UNITS_KEY = "targetUnits";
	private static final String ACTION_KEY = "action";
	private static final String COLUMN_KEY = "column";

	private static final String SHOW = "show";
	private static final String RESET = "reset";
	private static final String UNDO_REDO = "undo-redo";
	private static final String CHANGE_COLUMN_UNITS = "changeColumnUnits";
	private static final String CHANGE_ALL_UNITS = "changeAllUnits";
	private static final String SHOW_TABLE_COMMAND = "Show results table";
	private static final String DIGITAL = "digital";
	private static final String ANALOG = "analog";

	@Override
	public void run(String arg) {
		GUI.setLookAndFeel();
		//
		String options = Macro.getOptions();
		String action = options == null ? SHOW : Macro.getValue(options, ACTION_KEY, SHOW);
		try {
			IJResultsTable resultsTable = IJResultsTable.getResultsTable();
			if (SHOW.equals(action)) {
				resultsTable.show();
			}
			else if (RESET.equals(action)) {
				resultsTable.copyOriginalToActual();
				resultsTable.convertAllColumnsToAnalogUnits();
				resultsTable.getOperationHistoryPanel().removeAllOperations();
				resultsTable.setStatus("Results reset.");
				resultsTable.showPreview();
			}
			else if (UNDO_REDO.equals(action)) {
				resultsTable.getOperationHistoryPanel().undoOrRedoLastOperation();
			}
			else if (CHANGE_COLUMN_UNITS.equals(action)) {
				String column = Macro.getValue(options, COLUMN_KEY, "");
				if (!resultsTable.columnExists(column)) {
					throw new RuntimeException("Column \"" + column + "\" does not exist.");
				}
				MoleculeDescriptor.Units targetUnits = Units.fromString(Macro.getValue(options,
					TARGET_UNITS_KEY, ""));
				if (PSFModel.Params.LABEL_X.equals(column) || PSFModel.Params.LABEL_Y.equals(column)) {
					// ensure that X and Y are always in same units!
					resultsTable.model.setColumnUnits(PSFModel.Params.LABEL_X, targetUnits);
					resultsTable.model.setColumnUnits(PSFModel.Params.LABEL_Y, targetUnits);
				}
				else {
					resultsTable.model.setColumnUnits(column, targetUnits);
				}
			}
			else if (CHANGE_ALL_UNITS.equals(action)) {
				String analogOrDigital = Macro.getValue(options, TARGET_UNITS_KEY, ANALOG);
				if (analogOrDigital.equals(ANALOG)) {
					resultsTable.convertAllColumnsToAnalogUnits();
				}
				else if (analogOrDigital.equals(DIGITAL)) {
					resultsTable.convertAllColumnsToDigitalUnits();
				}
			}
			else {
				List<? extends PostProcessingModule> modules = resultsTable.getPostProcessingModules();

				PostProcessingModule selectedModule = null;
				for (PostProcessingModule module : modules) {
					if (module.getMacroName().equals(action)) {
						selectedModule = module;
						break;
					}
				}
				if (selectedModule != null) {
					selectedModule.run();
				}
				else {
					throw new IllegalArgumentException("Post processing module not found for action: " +
						action);
				}
			}

		}
		catch (Exception e) {
			IJ.handleException(e);
		}
	}

	public static void recordReset() {
		if (Recorder.record) {
			Recorder.setCommand(SHOW_TABLE_COMMAND);
			Recorder.recordOption(ACTION_KEY, RESET);
			Recorder.saveCommand();
		}
	}

	public static void recordUndoOrRedo() {
		if (Recorder.record) {
			Recorder.setCommand(SHOW_TABLE_COMMAND);
			Recorder.recordOption(ACTION_KEY, UNDO_REDO);
			Recorder.saveCommand();
		}
	}

	public static void recordChangeColumnUnits(String column, MoleculeDescriptor.Units targetUnits) {
		if (Recorder.record) {
			Recorder.setCommand(SHOW_TABLE_COMMAND);
			Recorder.recordOption(ACTION_KEY, CHANGE_COLUMN_UNITS);
			Recorder.recordOption(COLUMN_KEY, column);
			Recorder.recordOption(TARGET_UNITS_KEY, targetUnits.toString());
			Recorder.saveCommand();
		}
	}

	public static void recordChangeAllUnits(boolean analog) {
		if (Recorder.record) {
			Recorder.setCommand(SHOW_TABLE_COMMAND);
			Recorder.recordOption(ACTION_KEY, CHANGE_ALL_UNITS);
			Recorder.recordOption(TARGET_UNITS_KEY, analog ? ANALOG : DIGITAL);
			Recorder.saveCommand();
		}
	}
}
