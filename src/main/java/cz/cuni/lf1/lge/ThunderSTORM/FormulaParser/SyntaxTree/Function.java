package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.measure.Measurements;
import ij.process.FloatProcessor;
import ij.process.FloatStatistics;
import java.util.Arrays;
import java.util.HashSet;

public class Function extends Node {
    
    private String name = null;
    private Node arg = null;
    
    private static final HashSet<String> builtInFunctions = new HashSet<String>(
            Arrays.asList(new String[] {"var", "std", "mean", "median", "max", "min", "sum", "abs"}));

    @Override
    public void semanticScan() throws FormulaParserException {
        if(!builtInFunctions.contains(name.toLowerCase()))
            throw new FormulaParserException("Semantic error! Function '" + name + "' does not exist!");
        arg.semanticScan();
    }
    
    public Function(String fnName, Node argument) throws FormulaParserException {
        name = fnName;
        arg = argument;
    }
    
    @Override
    public RetVal eval(Object param) {
        if(name.equals("var")) return new RetVal(var(param));
        if(name.equals("std")) return new RetVal(std(param));
        if(name.equals("mean")) return new RetVal(mean(param));
        if(name.equals("median")) return new RetVal(median(param));
        if(name.equals("max")) return new RetVal(max(param));
        if(name.equals("min")) return new RetVal(min(param));
        if(name.equals("sum")) return new RetVal(sum(param));
        if(name.equals("abs")) return abs(param);
        // the following will never happen due to the semanticCheck in the constructor
        throw new FormulaParserException("Semantic error! Function '" + name + "' does not exist!");
    }
    
    protected double max(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return (FloatStatistics.getStatistics((FloatProcessor)val.get(), Measurements.MIN_MAX, null)).max;
        } else if(val.isVector()) {
            return VectorMath.max((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double min(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return (FloatStatistics.getStatistics((FloatProcessor)val.get(), Measurements.MIN_MAX, null)).min;
        } else if(val.isVector()) {
            return VectorMath.min((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double sum(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return VectorMath.sum((float [])((FloatProcessor)val.get()).getPixels());
        } else if(val.isVector()) {
            return VectorMath.sum((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double var(Object param) {
        return MathProxy.sqr(std(param));
    }
    
    protected double std(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return (FloatStatistics.getStatistics((FloatProcessor)val.get(), Measurements.STD_DEV, null)).stdDev;
        } else if(val.isVector()) {
            return VectorMath.stddev((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double mean(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return (FloatStatistics.getStatistics((FloatProcessor)val.get(), Measurements.MEAN, null)).mean;
        } else if(val.isVector()) {
            return VectorMath.mean((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double median(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return (FloatStatistics.getStatistics((FloatProcessor)val.get(), Measurements.MEDIAN, null)).median;
        } else if(val.isVector()) {
            return VectorMath.median((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected RetVal abs(Object param) {
        RetVal val = arg.eval(param);
        if(val.isMatrix()) {    // FloatProcessor
            return new RetVal(ImageMath.abs((FloatProcessor)val.get()));
        } else if(val.isVector()) { // Double []
            return new RetVal(VectorMath.abs((Double[])val.get()));
        } else if(val.isValue()) {    // Double
            return new RetVal(MathProxy.abs(((Double)val.get()).doubleValue()));
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }

}
