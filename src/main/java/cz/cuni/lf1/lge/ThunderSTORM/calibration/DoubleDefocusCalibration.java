package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import ij.IJ;
import org.yaml.snakeyaml.Yaml;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DoubleDefocusCalibration<T extends DefocusCalibration> extends DefocusCalibration {

    public T cal1, cal2;

    public DoubleDefocusCalibration(String calName, Homography.TransformationMatrix homography, T cal1, T cal2) {
        super(calName);
        this.homography = homography;
        this.cal1 = cal1;
        this.cal2 = cal2;
    }

    @Override
    public void saveToFile(String path) throws IOException {
        FileWriter fw = null;
        try {
            File file = new File(path);
            fw = new FileWriter(file);
            List<Object> objects = new ArrayList<Object>();
            objects.add(cal1);
            objects.add(cal2);
            if (homography != null) objects.add(homography);
            new Yaml(new Homography.TransformationMatrix.YamlRepresenter()).dumpAll(objects.iterator(), fw);
            IJ.log("Calibration file saved to: " + file.getAbsolutePath());
            IJ.showStatus("Calibration file saved to " + file.getAbsolutePath());
        } finally {
            if(fw != null) {
                fw.close();
            }
        }
    }

    // TODO: the following is just a dummy implementation which is not applicable for fitting!!!

    @Override
    public double evalDefocus(double z, double w0, double a, double b, double c, double d) {
        throw new NotImplementedException();
    }

    @Override
    public double evalDefocus2(double z, double w0, double a, double b, double c, double d) {
        throw new NotImplementedException();
    }

    @Override
    public double dwx(double z) {
        throw new NotImplementedException();
    }

    @Override
    public double dwy(double z) {
        throw new NotImplementedException();
    }

    @Override
    public double dwx2(double z) {
        throw new NotImplementedException();
    }

    @Override
    public double dwy2(double z) {
        throw new NotImplementedException();
    }

    @Override
    public DaostormCalibration getDaoCalibration() {
        throw new NotImplementedException();
    }
}
