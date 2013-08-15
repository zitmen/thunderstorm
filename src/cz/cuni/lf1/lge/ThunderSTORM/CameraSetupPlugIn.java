package cz.cuni.lf1.lge.ThunderSTORM;

import fiji.util.gui.GenericDialogPlus;
import ij.Macro;
import ij.Prefs;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class CameraSetupPlugIn implements PlugIn {
    
    public static double pixelSize = Defaults.PIXEL_SIZE.toDouble();
    public static double photons2ADU = Defaults.PHOTONS_PER_ADU.toDouble();
    public static double gain = Defaults.GAIN.toDouble();
    public static double offset = Defaults.OFFSET.toDouble();
    
    @Override
    public void run(String arg) {
        String macroOptions = Macro.getOptions();
        if(macroOptions != null) {
            readMacroOptions(macroOptions);
            return;
        }
        //
        final GenericDialogPlus gd = new GenericDialogPlus("Camera setup");
        gd.addNumericField("Pixel size [nm]: ", Defaults.PIXEL_SIZE.toDouble(), 1);
        gd.addNumericField("Photoelectrons per A/D count: ", Defaults.PHOTONS_PER_ADU.toDouble(), 1);
        gd.addNumericField("Gain: ", Defaults.GAIN.toDouble(), 1);
        gd.addNumericField("Base level [A/D counts]: ", Defaults.OFFSET.toDouble(), 1);
        gd.addButton("Reset to defaults", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToDefaults(gd);
            }
        });
        gd.showDialog();
        //
        if(!gd.wasCanceled()) {
            recordOptions();
            savePreferences();
            //
            pixelSize = Double.parseDouble(gd.getNextString());
            photons2ADU = Double.parseDouble(gd.getNextString());
            gain = Double.parseDouble(gd.getNextString());
            offset = Double.parseDouble(gd.getNextString());
        }
    }
    
    public static double pixelsToNanometers(double pixels) {
        return pixels * pixelSize;
    }
    
    public static double nanometersToPixels(double nanometers) {
        return nanometers / pixelSize;
    }
    
    public static double adCountsToPhotons(double counts) {
        return (counts - offset) * photons2ADU / gain;
    }
    
    public static double photonsToAdCounts(double photons) {
        return photons / photons2ADU * gain + offset;
    }
    
    public static double peakEnergyToPhotons(double energy) {
        return energy * photons2ADU / gain;
    }
    
    public static double photonsToPeakEnergy(double photons) {
        return photons * gain / photons2ADU;
    }
    
    public static enum Defaults {
        // from ANDOR iXon EM+ performance sheet
        PIXEL_SIZE(80.0),   // nm per px
        PHOTONS_PER_ADU(3.6), // ccd sensitivity
        GAIN(100.0),
        OFFSET(414.0);   // base level (counts)
        
        
        private double value;
        
        private Defaults(double val) {
            value = val;
        }
        
        public double toDouble() {
            return value;
        }
        
        @Override
        public String toString() {
            return Double.toString(value);
        }
    }
    
    public static void savePreferences() {
        Prefs.set("thunderstorm.camera.pixelsize", CameraSetupPlugIn.pixelSize);
        Prefs.set("thunderstorm.camera.photons2adu", CameraSetupPlugIn.photons2ADU);
        Prefs.set("thunderstorm.camera.gain", CameraSetupPlugIn.gain);
        Prefs.set("thunderstorm.camera.offset", CameraSetupPlugIn.offset);
    }

    public static void recordOptions() {
        Recorder.recordOption("pixelsize", Double.toString(CameraSetupPlugIn.pixelSize));
        Recorder.recordOption("photons2adu", Double.toString(CameraSetupPlugIn.photons2ADU));
        Recorder.recordOption("gain", Double.toString(CameraSetupPlugIn.gain));
        Recorder.recordOption("offset", Double.toString(CameraSetupPlugIn.offset));
    }

    public static void readMacroOptions(String options) {
        CameraSetupPlugIn.pixelSize = Double.parseDouble(Macro.getValue(options, "pixelsize", Defaults.PIXEL_SIZE.toString()));
        CameraSetupPlugIn.photons2ADU = Double.parseDouble(Macro.getValue(options, "photons2adu", Defaults.PHOTONS_PER_ADU.toString()));
        CameraSetupPlugIn.gain = Double.parseDouble(Macro.getValue(options, "gain", Defaults.GAIN.toString()));
        CameraSetupPlugIn.offset = Double.parseDouble(Macro.getValue(options, "offset", Defaults.OFFSET.toString()));
    }

    public void resetToDefaults(GenericDialogPlus gd) {
        CameraSetupPlugIn.pixelSize = Defaults.PIXEL_SIZE.toDouble();
        CameraSetupPlugIn.photons2ADU = Defaults.PHOTONS_PER_ADU.toDouble();
        CameraSetupPlugIn.gain = Defaults.GAIN.toDouble();
        CameraSetupPlugIn.offset = Defaults.OFFSET.toDouble();
        //
        Vector<TextField> fields = (Vector<TextField>)gd.getNumericFields();
        fields.elementAt(0).setText(Defaults.PIXEL_SIZE.toString());
        fields.elementAt(1).setText(Defaults.PHOTONS_PER_ADU.toString());
        fields.elementAt(2).setText(Defaults.GAIN.toString());
        fields.elementAt(3).setText(Defaults.OFFSET.toString());
    }

}
