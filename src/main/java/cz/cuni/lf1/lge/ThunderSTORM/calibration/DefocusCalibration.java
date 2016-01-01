package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import ij.IJ;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class DefocusCalibration {

    public String name;
    public double angle;
    public double w01, w02;
    public double a1, a2;
    public double b1, b2;
    public double c1, c2;
    public double d1, d2;

    public transient Homography.TransformationMatrix homography;

    public DefocusCalibration(String calName) {
        name = calName;
        homography = null;
        angle = 0.0;
        w01 = w02 = 0.0;
        a1 = a2 = 0.0;
        b1 = b2 = 0.0;
        c1 = c2 = 0.0;
        d1 = d2 = 0.0;
    }

    public DefocusCalibration(String calName, double angle, Homography.TransformationMatrix homography, double w01, double a1, double b1, double c1, double d1, double w02, double a2, double b2, double c2, double d2) {
        name = calName;
        this.angle = angle;
        this.homography = homography;
        this.w01 = w01;
        this.a1 = a1;
        this.b1 = b1;
        this.c1 = c1;
        this.d1 = d1;
        this.w02 = w02;
        this.a2 = a2;
        this.b2 = b2;
        this.c2 = c2;
        this.d2 = d2;
    }

    public abstract double evalDefocus(double z, double w0, double a, double b, double c, double d);
    public abstract double evalDefocus2(double z, double w0, double a, double b, double c, double d);
    public abstract double dwx(double z);
    public abstract double dwy(double z);
    public abstract double dwx2(double z);
    public abstract double dwy2(double z);

    public String getName() {
        return name;
    }

    public double getSigma1(double z) {
        return evalDefocus(z, w01, a1, b1, c1, d1);
    }

    public double getSigma2(double z) {
        return evalDefocus(z, w02, a2, b2, c2, d2);
    }

    public double getSigma1Squared(double z) {
        return evalDefocus2(z, w01, a1, b1, c1, d1);
    }

    public double getSigma2Squared(double z) {
        return evalDefocus2(z, w02, a2, b2, c2, d2);
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getA1() {
        return a1;
    }

    public void setA1(double a1) {
        this.a1 = a1;
    }

    public double getW01() {
        return w01;
    }

    public void setW01(double w01) {
        this.w01 = w01;
    }

    public double getB1() {
        return b1;
    }

    public void setB1(double b1) {
        this.b1 = b1;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getA2() {
        return a2;
    }

    public void setA2(double a2) {
        this.a2 = a2;
    }

    public double getB2() {
        return b2;
    }

    public void setB2(double b2) {
        this.b2 = b2;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    public double getD1() {
        return d1;
    }

    public void setD1(double d1) {
        this.d1 = d1;
    }

    public double getD2() {
        return d2;
    }

    public void setD2(double d2) {
        this.d2 = d2;
    }

    public double getW02() {
        return w02;
    }

    public void setW02(double w02) {
        this.w02 = w02;
    }

    // Daostorm calibration model contains D (depth of field) information,
    // which is necessary for calculation of uncertainty of an estimator
    public abstract DaostormCalibration getDaoCalibration();

    public void saveToFile(String path) throws IOException {
        FileWriter fw = null;
        try {
            File file = new File(path);
            fw = new FileWriter(file);
            List<Object> objects = new ArrayList<Object>();
            objects.add(this);
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
}
