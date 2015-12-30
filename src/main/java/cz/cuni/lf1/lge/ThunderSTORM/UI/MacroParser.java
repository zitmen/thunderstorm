package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IBiplaneEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import ij.Executer;
import ij.Macro;
import ij.plugin.frame.Recorder;
import java.util.List;

public class MacroParser {

    String options;
    private static final String FILTER_KEY = "filter";
    private static final String DETECTOR_KEY = "detector";
    private static final String ESTIMATOR_KEY = "estimator";
    private static final String ESTIMATOR_BIPLANE_KEY = "estimator.biplane";
    private static final String RENDERER_KEY = "renderer";
    List<IFilterUI> knownFilters;
    List<IEstimatorUI> knownEstimators;
    List<IBiplaneEstimatorUI> knownBiplaneEstimators;
    List<IDetectorUI> knownDetectors;
    List<IRendererUI> knownRenderers;
    int selectedFilterIndex = -1;
    int selectedDetectorIndex = -1;
    int selectedEstimatorIndex = -1;
    int selectedBiplaneEstimatorIndex = -1;
    int selectedRendererIndex = -1;

    @SuppressWarnings("unchecked")
    public MacroParser(boolean isBiplane,
                       List<IFilterUI> knowFilters,
                       List knowEstimators,
                       List<IDetectorUI> knowDetectors,
                       List<IRendererUI> knowRenderers) {
        this.knownFilters = knowFilters;
        if (isBiplane) {
            this.knownEstimators = (List<IEstimatorUI>) knowEstimators;
        } else {
            this.knownBiplaneEstimators = (List<IBiplaneEstimatorUI>) knowEstimators;
        }
        this.knownDetectors = knowDetectors;
        this.knownRenderers = knowRenderers;
        options = Macro.getOptions();
        if(options == null) {
            throw new MacroException("No macro options.");
        }
    }

    public IFilterUI getFilterUI() {
        return knownFilters.get(getFilterIndex());
    }

    public int getFilterIndex() {
        if(selectedFilterIndex < 0) {
            int index = getModuleIndex(knownFilters, FILTER_KEY);
            selectedFilterIndex = index;
            knownFilters.get(index).readMacroOptions(options);
            return index;
        } else {
            return selectedFilterIndex;
        }
    }

    public IEstimatorUI getEstimatorUI() {
        return knownEstimators.get(getEstimatorIndex());
    }

    public int getEstimatorIndex() {
        if(selectedEstimatorIndex < 0) {
            int index = getModuleIndex(knownEstimators, ESTIMATOR_KEY);
            selectedEstimatorIndex = index;
            knownEstimators.get(index).readMacroOptions(options);
            return index;
        } else {
            return selectedEstimatorIndex;
        }
    }

    public int getBiplaneEstimatorIndex() {
        if(selectedBiplaneEstimatorIndex < 0) {
            int index = getModuleIndex(knownBiplaneEstimators, ESTIMATOR_BIPLANE_KEY);
            selectedBiplaneEstimatorIndex = index;
            knownBiplaneEstimators.get(index).readMacroOptions(options);
            return index;
        } else {
            return selectedBiplaneEstimatorIndex;
        }
    }

    public IDetectorUI getDetectorUI() {
        return knownDetectors.get(getDetectorIndex());
    }

    public int getDetectorIndex() {
        if(selectedDetectorIndex < 0) {
            int index = getModuleIndex(knownDetectors, DETECTOR_KEY);
            selectedDetectorIndex = index;
            knownDetectors.get(index).readMacroOptions(options);
            return index;
        } else {
            return selectedDetectorIndex;
        }
    }

    public IRendererUI getRendererUI() {
        return knownRenderers.get(getRendererIndex());
    }

    public int getRendererIndex() {
        if(selectedRendererIndex < 0) {
            int index = getModuleIndex(knownRenderers, RENDERER_KEY);
            selectedRendererIndex = index;
            knownRenderers.get(index).readMacroOptions(options);
            return index;
        } else {
            return selectedRendererIndex;
        }
    }

    public <T extends IModuleUI<?>> int getModuleIndex(List<T> knownModules, String moduleKey) {
        String moduleName = Macro.getValue(options, moduleKey, null);
        if(moduleName == null) {
            throw new MacroException("No module specified: " + moduleKey);
        }
        for(int i = 0; i < knownModules.size(); i++) {

            if(knownModules.get(i).getName().equalsIgnoreCase(moduleName)) {
                return i;
            }
        }
        throw new MacroException("Module not found: " + moduleName);
    }

    public static void recordFilterUI(IFilterUI filter) {
        Recorder.recordOption(FILTER_KEY, filter.getName());
        filter.recordOptions();
    }

    public static void recordDetectorUI(IDetectorUI detector) {
        Recorder.recordOption(DETECTOR_KEY, detector.getName());
        detector.recordOptions();
    }

    public static void recordEstimatorUI(IEstimatorUI estimator) {
        Recorder.recordOption(ESTIMATOR_KEY, estimator.getName());
        estimator.recordOptions();
    }

    public static void recordBiplaneEstimatorUI(IBiplaneEstimatorUI biplaneEstimator) {
        Recorder.recordOption(ESTIMATOR_BIPLANE_KEY, biplaneEstimator.getName());
        biplaneEstimator.recordOptions();
    }

    public static void recordRendererUI(IRendererUI renderer) {
        Recorder.recordOption(RENDERER_KEY, renderer.getName());
        renderer.recordOptions();
    }

    public static boolean isRanFromMacro() {
        return Macro.getOptions() != null;
    }
    
    public static void runNestedWithRecording(String command, String options){
        String oldCommand = Recorder.getCommand();
        
        Recorder.setCommand(command);
        Macro.setOptions(options);
        Executer ex = new Executer(command);
        ex.run();
        
        Recorder.setCommand(oldCommand);
    }
}
