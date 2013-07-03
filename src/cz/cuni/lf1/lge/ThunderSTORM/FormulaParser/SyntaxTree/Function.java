package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.util.Math;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import ij.process.FloatProcessor;
import java.util.Arrays;
import java.util.HashSet;

public class Function extends Node {
    
    private String name = null;
    private Node arg = null;
    
    private static final HashSet<String> builtInFunctions = new HashSet<String>(
            Arrays.asList(new String[] {"var", "std", "mean", "median", "max", "min", "sum"}));

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
    public RetVal eval() {
        if(name.equals("var")) return new RetVal(var());
        if(name.equals("std")) return new RetVal(std());
        if(name.equals("mean")) return new RetVal(mean());
        if(name.equals("median")) return new RetVal(median());
        if(name.equals("max")) return new RetVal(max());
        if(name.equals("min")) return new RetVal(min());
        if(name.equals("sum")) return new RetVal(sum());
        // the following will never happen due to the semanticCheck in the constructor
        throw new FormulaParserException("Semantic error! Function '" + name + "' does not exist!");
    }
    
    protected double max() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return ((FloatProcessor)val.get()).getStatistics().max;
        } else if(val.isVector()) {
            return Math.max((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double min() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return ((FloatProcessor)val.get()).getStatistics().min;
        } else if(val.isVector()) {
            return Math.min((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double sum() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return Math.sum((float [])((FloatProcessor)val.get()).getPixels());
        } else if(val.isVector()) {
            return Math.sum((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double var() {
        return Math.sqr(std());
    }
    
    protected double std() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return ((FloatProcessor)val.get()).getStatistics().stdDev;
        } else if(val.isVector()) {
            return Math.stddev((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double mean() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return ((FloatProcessor)val.get()).getStatistics().mean;
        } else if(val.isVector()) {
            return Math.mean((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }
    
    protected double median() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return ((FloatProcessor)val.get()).getStatistics().median;
        } else if(val.isVector()) {
            return Math.median((Number[])val.get()).doubleValue();
        } else if(val.isValue()) {    // Double
            return ((Double)val.get()).doubleValue();
        }
        throw new FormulaParserException("Variables can be only scalars, vectors, or matrices!");
    }

}
