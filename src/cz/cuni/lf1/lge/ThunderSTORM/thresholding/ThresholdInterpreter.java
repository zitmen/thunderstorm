package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;

class ThresholdInterpreter {
    
    private Node tree;
    
    public ThresholdInterpreter(String formula) throws FormulaParserException {
        tree = new FormulaParser(formula, FormulaParser.FORMULA_THRESHOLD).parse();
        tree.semanticScan();
    }
    
    public float evaluate() throws FormulaParserException {
        RetVal retval = tree.eval();
        if(!retval.isValue())
            throw new FormulaParserException("Semantic error: result of threshold formula must be a scalar value!");
        return ((Number)(retval.get())).floatValue();
    }

}
