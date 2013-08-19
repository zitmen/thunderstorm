package cz.cuni.lf1.lge.ThunderSTORM.results;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.OperationsHistoryPanel.Operation;
import ij.IJ;
import ij.ImagePlus;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MeasurementProtocol {
    
    public HashMap<String, Object> imageInfo;
    public HashMap<String, Object> cameraSettings;
    public IFilterUI filter;
    public IDetectorUI detector;
    public IEstimatorUI estimator;
    
    public MeasurementProtocol() {
        this.imageInfo = null;
        this.cameraSettings = null;
        this.filter = null;
        this.detector = null;
        this.estimator = null;
    }
    
    public MeasurementProtocol(ImagePlus analyzedImage, IFilterUI filter, IDetectorUI detector, IEstimatorUI estimator) {
        this.imageInfo = getImageInfo(analyzedImage);
        this.cameraSettings = CameraSetupPlugIn.exportSettings();
        this.filter = filter;
        this.detector = detector;
        this.estimator = estimator;
    }
    
    public void export(String fpath) {
        assert(fpath != null);
        assert(!fpath.trim().isEmpty());
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fpath));
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            writer.write("Input:");
            writer.newLine();
            writer.write(gson.toJson(imageInfo));
            writer.newLine();
            writer.write("Camera settings:");
            writer.newLine();
            writer.write(gson.toJson(cameraSettings));
            writer.newLine();
            writer.write("Analysis:");
            writer.newLine();
            writer.write(gson.toJson(filter));
            writer.newLine();
            writer.write(gson.toJson(detector));
            writer.newLine();
            writer.write(gson.toJson(estimator));
            writer.newLine();
            writer.write("Post-processing:");
            writer.newLine();
            writer.write(gson.toJson(IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory(), new TypeToken<List<Operation>>(){}.getType()));
            writer.newLine();
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

    private HashMap<String,Object> getImageInfo(ImagePlus img) {
        HashMap<String,Object> info = new HashMap<String,Object>();
        info.put("title", img.getTitle());
        info.put("roiBounds", img.getRoi() == null ? null : img.getRoi().getBounds());
        return info;
    }
    
}
