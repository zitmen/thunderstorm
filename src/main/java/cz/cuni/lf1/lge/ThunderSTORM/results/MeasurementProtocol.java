package cz.cuni.lf1.lge.ThunderSTORM.results;

import com.google.gson.*;
import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ModuleLoader;
import cz.cuni.lf1.lge.ThunderSTORM.ThunderSTORM;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.EmptyDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.EmptyEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.EllipticGaussianEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IBiplaneEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.EmptyFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.OperationsHistoryPanel.Operation;
import cz.cuni.lf1.lge.ThunderSTORM.results.PostProcessingModule.DefaultOperation;
import ij.IJ;
import ij.ImagePlus;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class MeasurementProtocol {

    public String version;
    public HashMap<String, Object> imageInfo;
    public HashMap<String, Object> cameraSettings;
    public IFilterUI analysisFilter;
    public IDetectorUI analysisDetector;
    public IEstimatorUI analysisEstimator;
    public IBiplaneEstimatorUI analysisBiplaneEstimator;
    public List<Operation> postProcessing;

    private boolean is3d;
    private boolean isSet3d;
    
    public MeasurementProtocol() {
        this.version = "ThunderSTORM (" + ThunderSTORM.VERSION + ")";
        this.imageInfo = new HashMap<String, Object>();
        this.cameraSettings = new HashMap<String, Object>();
        this.analysisFilter = new EmptyFilter();
        this.analysisDetector = new EmptyDetector();
        this.analysisEstimator = new EmptyEstimator();
        this.postProcessing = IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory();
        this.isSet3d = false;
    }
    
    public MeasurementProtocol(ImagePlus analyzedImage, IFilterUI filter, IDetectorUI detector, IEstimatorUI estimator) {
        this.version = "ThunderSTORM (" + ThunderSTORM.VERSION + ")";
        this.imageInfo = getImageInfo(analyzedImage);
        this.cameraSettings = CameraSetupPlugIn.exportSettings();
        this.analysisFilter = filter;
        this.analysisDetector = detector;
        this.analysisEstimator = estimator;
        this.postProcessing = IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory();
        this.isSet3d = false;
    }

    public MeasurementProtocol(ImagePlus analyzedPlane1, ImagePlus analyzedPlane2, IFilterUI filter, IDetectorUI detector, IBiplaneEstimatorUI biplaneEstimator) {
        this.version = "ThunderSTORM (" + ThunderSTORM.VERSION + ")";
        this.imageInfo = getImageInfo(analyzedPlane1);  // TODO: append the second image info!!!
        this.cameraSettings = CameraSetupPlugIn.exportSettings();
        this.analysisFilter = filter;
        this.analysisDetector = detector;
        this.analysisBiplaneEstimator = biplaneEstimator;
        this.postProcessing = IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory();
        this.isSet3d = false;
    }

    public boolean is3D() {
        // this is used as an indicator whether to calculate the z-uncertainty;
        // since we don't have a implementation for biplane, the function returns
        // tru only for astigmatism
        if (!this.isSet3d) {
            this.is3d = ((analysisEstimator != null) && (analysisEstimator instanceof EllipticGaussianEstimatorUI));
            this.isSet3d = true;
        }
        return this.is3d;
    }
    
    public void exportToFile(String fpath) {
        assert(fpath != null);
        assert(!fpath.trim().isEmpty());

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fpath));
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            this.postProcessing = IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory(); // update
            writer.write(fixNewLines(gson.toJson(this)));
        } catch(IOException ex) {
            IJ.handleException(ex);
        } finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            } catch(IOException ex) {
                IJ.handleException(ex);
            }
        }
    }

    public static MeasurementProtocol importFromFile(String fpath) {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(IFilterUI.class, new FilterAdapter());
        gson.registerTypeAdapter(IDetectorUI.class, new DetectorAdapter());
        gson.registerTypeAdapter(IEstimatorUI.class, new EstimatorAdapter());
        gson.registerTypeAdapter(IBiplaneEstimatorUI.class, new BiplaneEstimatorAdapter());
        gson.registerTypeAdapter(DefocusCalibration.class, new CylindricalLensCalibrationAdapter());
        gson.registerTypeAdapter(Operation.class, new OperationAdapter());
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fpath));
            return gson.create().fromJson(reader, MeasurementProtocol.class);
        } catch (FileNotFoundException ex) {
            IJ.showMessage("Measurement protocol can't be opened!");
            IJ.handleException(ex);
        } catch (JsonParseException ex) {
            IJ.showMessage("Invalid format of measurement protocol: parsing error!");
            IJ.handleException(ex);
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch(IOException ex) {
                IJ.handleException(ex);
            }
        }
        return new MeasurementProtocol();
    }

    private HashMap<String,Object> getImageInfo(ImagePlus img) {
        HashMap<String,Object> info = new HashMap<String,Object>();
        info.put("title", img.getTitle());
        info.put("roiBounds", img.getRoi() == null ? null : img.getRoi().getBounds());
        return info;
    }

    private String fixNewLines(String json) {
        String fixed = json.replace("\r\n", "\n").replace("\r", "\n");  // convert Windows and Mac to Unix
        // now convert to the coding dependant on the OS
        if(SystemUtils.IS_OS_WINDOWS) { // Windows
            return fixed.replace("\n", "\r\n");
        } else if(SystemUtils.IS_OS_MAC) {  // Mac OS
            return fixed.replace("\n", "\r");
        } else {    // Unix-based systems
            return fixed;   // "\n"
        }
    }

    private static class FilterAdapter implements JsonDeserializer<IFilterUI> {
        @Override
        public IFilterUI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("name").getAsString();
            for (IFilterUI filter : ModuleLoader.getUIModules(IFilterUI.class)) {
                if (name.equals(filter.getName())) {
                    return context.deserialize(json, filter.getClass());
                }
            }
            throw new JsonParseException("Unknown filter \"" + name + "\"!");
        }
    }

    private static class DetectorAdapter implements JsonDeserializer<IDetectorUI> {
        @Override
        public IDetectorUI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("name").getAsString();
            for (IDetectorUI detector : ModuleLoader.getUIModules(IDetectorUI.class)) {
                if (name.equals(detector.getName())) {
                    return context.deserialize(json, detector.getClass());
                }
            }
            throw new JsonParseException("Unknown detector \"" + name + "\"!");
        }
    }

    private static class EstimatorAdapter implements JsonDeserializer<IEstimatorUI> {
        @Override
        public IEstimatorUI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("name").getAsString();
            for (IEstimatorUI estimator : ModuleLoader.getUIModules(IEstimatorUI.class)) {
                if (name.equals(estimator.getName())) {
                    return context.deserialize(json, estimator.getClass());
                }
            }
            throw new JsonParseException("Unknown estimator \"" + name + "\"!");
        }
    }

    private static class BiplaneEstimatorAdapter implements JsonDeserializer<IBiplaneEstimatorUI> {
        @Override
        public IBiplaneEstimatorUI deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("name").getAsString();
            for (IBiplaneEstimatorUI biplaneEstimator : ModuleLoader.getUIModules(IBiplaneEstimatorUI.class)) {
                if (name.equals(biplaneEstimator.getName())) {
                    return context.deserialize(json, biplaneEstimator.getClass());
                }
            }
            throw new JsonParseException("Unknown estimator \"" + name + "\"!");
        }
    }

    private static class CylindricalLensCalibrationAdapter implements JsonDeserializer<DefocusCalibration> {
        @Override
        public DefocusCalibration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String name = json.getAsJsonObject().get("name").getAsString();
            for (DefocusFunction defocusFn : ModuleLoader.getUIModules(DefocusFunction.class)) {
                if (name.equals(defocusFn.getName())) {
                    return context.deserialize(json, defocusFn.getCalibration().getClass());
                }
            }
            throw new JsonParseException("Unknown calibration model \"" + name + "\"!");
        }
    }

    private static class OperationAdapter implements JsonDeserializer<Operation> {
        @Override
        public Operation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return context.deserialize(json, DefaultOperation.class);
        }
    }
}
