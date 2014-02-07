package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;

public class Range {

    public double from;
    public double to;
    public double step;
    
    public Range() {
        from = to = step = 0;
    }
    
    public Range(double from, double to) {
        this.from = from;
        this.to = to;
        this.step = 1;
    }
    
    public Range(double from, double step, double to) {
        this.from = from;
        this.step = step;
        this.to = to;
    }
    
    public static Range parseFromStepTo(String rangeText) throws RuntimeException {
        String [] ft = rangeText.split(":");
        try {
            if((ft == null) || (ft.length != 3)) throw new Exception();
            return new Range(Double.parseDouble(ft[0]), Double.parseDouble(ft[1]), Double.parseDouble(ft[2]));
        } catch(Exception ex) {
            throw new RuntimeException("Wrong format of range field.");
        }
    }
    
    public static Range parseFromTo(String rangeText) throws RuntimeException {
        String [] ft = rangeText.split(":");
        try {
            if((ft == null) || (ft.length != 2)) throw new Exception();
            return new Range(Double.parseDouble(ft[0]), Double.parseDouble(ft[1]));
        } catch(Exception ex) {
            throw new RuntimeException("Wrong format of range field.");
        }
    }
    
    public boolean isIn(double value) {
        return ((from <= value) && (value <= to));
    }
    
    public void scale(double factor) {
        from *= factor;
        step *= factor;
        to *= factor;
    }
    
    public void convert(MoleculeDescriptor.Units current, MoleculeDescriptor.Units target) {
        from = current.convertTo(target, from);
        step = current.convertTo(target, step);
        to = current.convertTo(target, to);
    }
    
    public static Range parseFromTo(String rangeText, MoleculeDescriptor.Units current, MoleculeDescriptor.Units target) {
        Range r = Range.parseFromTo(rangeText);
        r.convert(current, target);
        return r;
    }
    
    public static Range parseFromStepTo(String rangeText, MoleculeDescriptor.Units current, MoleculeDescriptor.Units target) {
        Range r = Range.parseFromStepTo(rangeText);
        r.convert(current, target);
        return r;
    }
    
    public String toStrFromStepTo() {
        return Double.toString(from) + ":" + Double.toString(step) + ":" + Double.toString(to);
    }
    
    public String toStrFromTo() {
        return Double.toString(from) + ":" + Double.toString(to);
    }
}
