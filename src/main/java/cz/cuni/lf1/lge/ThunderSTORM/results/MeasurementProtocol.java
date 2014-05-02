package cz.cuni.lf1.lge.ThunderSTORM.results;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ThunderSTORM;
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
import org.apache.commons.lang3.SystemUtils;

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
            writer.write("ThunderSTORM (" + ThunderSTORM.VERSION + ")");
            writer.newLine();
            writer.newLine();
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            writer.write("Input:");
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(imageInfo)));
            writer.newLine();
            writer.write("Camera settings:");
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(cameraSettings)));
            writer.newLine();
            writer.write("Analysis:");
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(filter)));
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(detector)));
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(estimator)));
            writer.newLine();
            writer.write("Post-processing:");
            writer.newLine();
            writer.write(fixNewLines(gson.toJson(IJResultsTable.getResultsTable().getOperationHistoryPanel().getHistory(), new TypeToken<List<Operation>>(){}.getType())));
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
    
}
