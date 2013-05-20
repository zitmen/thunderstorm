package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import ij.process.FloatProcessor;
import java.util.Arrays;
import java.util.HashSet;

public class Function extends Node {
    
    private String name = null;
    private Node arg = null;
    
    private static final HashSet<String> builtInFunctions = new HashSet<String>(
            Arrays.asList(new String[] {"var", "std", "mean", "med"}));

    @Override
    public void semanticScan() throws ThresholdFormulaException {
        if(!builtInFunctions.contains(name.toLowerCase()))
            throw new ThresholdFormulaException("Semantic error! Function '" + name + "' does not exist!");
        arg.semanticScan();
    }
    
    public Function(String fnName, Node argument) throws ThresholdFormulaException {
        name = fnName;
        arg = argument;
    }
    
    @Override
    public RetVal eval() {
        if(name.equals("var")) return new RetVal(var());
        if(name.equals("std")) return new RetVal(std());
        if(name.equals("mean")) return new RetVal(var());
        if(name.equals("med")) return new RetVal(var());
        // the following will never happen due to the semanticCheck in the constructor
        assert(true);
        return null;
    }
    
    protected float var() {
        return (float)sqr(std());
    }
    
    protected float std() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return (float)(((FloatProcessor)val.get()).getStatistics().stdDev);
        } else if(val.isValue()) {    // Float
            return ((Float)val.get()).floatValue();
        }
        assert(true);   // it is either scalar, or matrix; nothing else...
        return 0.0f;
    }
    
    protected float mean() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return (float)(((FloatProcessor)val.get()).getStatistics().mean);
        } else if(val.isValue()) {    // Float
            return ((Float)val.get()).floatValue();
        }
        assert(true);   // it is either scalar, or matrix; nothing else...
        return 0.0f;
    }
    
    protected float med() {
        RetVal val = arg.eval();
        if(val.isMatrix()) {    // FloatProcessor
            return (float)(((FloatProcessor)val.get()).getStatistics().median);
        } else if(val.isValue()) {    // Float
            return ((Float)val.get()).floatValue();
        }
        assert(true);   // it is either scalar, or matrix; nothing else...
        return 0.0f;
    }

}
