package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree.RetVal;

class ThresholdInterpreter {
    
    private Node tree;
    
    public ThresholdInterpreter(String formula) throws ThresholdFormulaException {
        tree = new FormulaParser(formula).parse();
        tree.semanticScan();
    }
    
    public float evaluate() throws ThresholdFormulaException {
        RetVal retval = tree.eval();
        if(!retval.isValue())
            throw new ThresholdFormulaException("Semantic error: result of threshold formula must be a scalar value!");
        return ((Float)(retval.get())).floatValue();
    }

}
