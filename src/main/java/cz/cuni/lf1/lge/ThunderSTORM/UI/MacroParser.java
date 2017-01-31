package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.DetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BaseEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.BiplaneEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.EstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.FilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.RendererUI;
import ij.Executer;
import ij.Macro;
import ij.plugin.frame.Recorder;

public class MacroParser {

    String options;
    private static final String FILTER_KEY = "filter";
    private static final String DETECTOR_KEY = "detector";
    private static final String ESTIMATOR_KEY = "estimator";
    private static final String ESTIMATOR_BIPLANE_KEY = "estimator.biplane";
    private static final String RENDERER_KEY = "renderer";
    FilterUI[] knownFilters;
    EstimatorUI[] knownEstimators;
    BiplaneEstimatorUI[] knownBiplaneEstimators;
    DetectorUI[] knownDetectors;
    RendererUI[] knownRenderers;
    int selectedFilterIndex = -1;
    int selectedDetectorIndex = -1;
    int selectedEstimatorIndex = -1;
    int selectedBiplaneEstimatorIndex = -1;
    int selectedRendererIndex = -1;

    @SuppressWarnings("unchecked")
    public MacroParser(boolean isBiplane,
                       FilterUI[] knowFilters,
                       BaseEstimatorUI[] knowEstimators,
                       DetectorUI[] knowDetectors,
                       RendererUI[] knowRenderers) {
        this.knownFilters = knowFilters;
        if (isBiplane) {
            this.knownBiplaneEstimators = (BiplaneEstimatorUI[]) knowEstimators;
        } else {
            this.knownEstimators = (EstimatorUI[]) knowEstimators;
        }
        this.knownDetectors = knowDetectors;
        this.knownRenderers = knowRenderers;
        options = Macro.getOptions();
        if(options == null) {
            throw new MacroException("No macro options.");
        }
    }

    public FilterUI getFilterUI() {
        return knownFilters[getFilterIndex()];
    }

    public int getFilterIndex() {
        if(selectedFilterIndex < 0) {
            int index = getModuleIndex(knownFilters, FILTER_KEY);
            selectedFilterIndex = index;
            knownFilters[index].readMacroOptions(options);
            return index;
        } else {
            return selectedFilterIndex;
        }
    }

    public EstimatorUI getEstimatorUI() {
        return knownEstimators[getEstimatorIndex()];
    }

    public int getEstimatorIndex() {
        if(selectedEstimatorIndex < 0) {
            int index = getModuleIndex(knownEstimators, ESTIMATOR_KEY);
            selectedEstimatorIndex = index;
            knownEstimators[index].readMacroOptions(options);
            return index;
        } else {
            return selectedEstimatorIndex;
        }
    }

    public int getBiplaneEstimatorIndex() {
        if(selectedBiplaneEstimatorIndex < 0) {
            int index = getModuleIndex(knownBiplaneEstimators, ESTIMATOR_BIPLANE_KEY);
            selectedBiplaneEstimatorIndex = index;
            knownBiplaneEstimators[index].readMacroOptions(options);
            return index;
        } else {
            return selectedBiplaneEstimatorIndex;
        }
    }

    public DetectorUI getDetectorUI() {
        return knownDetectors[getDetectorIndex()];
    }

    public int getDetectorIndex() {
        if(selectedDetectorIndex < 0) {
            int index = getModuleIndex(knownDetectors, DETECTOR_KEY);
            selectedDetectorIndex = index;
            knownDetectors[index].readMacroOptions(options);
            return index;
        } else {
            return selectedDetectorIndex;
        }
    }

    public RendererUI getRendererUI() {
        return knownRenderers[getRendererIndex()];
    }

    public int getRendererIndex() {
        if(selectedRendererIndex < 0) {
            int index = getModuleIndex(knownRenderers, RENDERER_KEY);
            selectedRendererIndex = index;
            knownRenderers[index].readMacroOptions(options);
            return index;
        } else {
            return selectedRendererIndex;
        }
    }

    public <T extends ModuleUI<?>> int getModuleIndex(T[] knownModules, String moduleKey) {
        String moduleName = Macro.getValue(options, moduleKey, null);
        if(moduleName == null) {
            throw new MacroException("No module specified: " + moduleKey);
        }
        for(int i = 0; i < knownModules.length; i++) {

            if(knownModules[i].getName().equalsIgnoreCase(moduleName)) {
                return i;
            }
        }
        throw new MacroException("Module not found: " + moduleName);
    }

    public static void recordFilterUI(FilterUI filter) {
        Recorder.recordOption(FILTER_KEY, filter.getName());
        filter.recordOptions();
    }

    public static void recordDetectorUI(DetectorUI detector) {
        Recorder.recordOption(DETECTOR_KEY, detector.getName());
        detector.recordOptions();
    }

    public static void recordEstimatorUI(EstimatorUI estimator) {
        Recorder.recordOption(ESTIMATOR_KEY, estimator.getName());
        estimator.recordOptions();
    }

    public static void recordBiplaneEstimatorUI(BiplaneEstimatorUI biplaneEstimator) {
        Recorder.recordOption(ESTIMATOR_BIPLANE_KEY, biplaneEstimator.getName());
        biplaneEstimator.recordOptions();
    }

    public static void recordRendererUI(RendererUI renderer) {
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
