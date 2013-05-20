package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;

public class Operator extends Node {
    
    public static final int ADD = 1;
    public static final int SUB = 2;
    public static final int MUL = 3;
    public static final int DIV = 4;
    public static final int POW = 5;
    
    int op;
    Node left = null, right = null;
    
    public Operator(int operator, Node leftOperand, Node rightOperand) {
        op = operator;
        left = leftOperand;
        right = rightOperand;
    }

    @Override
    public RetVal eval() {
        switch(op) {
            case ADD: return left.eval().add(right.eval());
            case SUB: return left.eval().sub(right.eval());
            case MUL: return left.eval().mul(right.eval());
            case DIV: return left.eval().div(right.eval());
            case POW: return left.eval().pow(right.eval());
            default : throw new UnsupportedOperationException("Unknown operator! Only supported operators are: + - * / ^");
        }
    }

    @Override
    public void semanticScan() throws ThresholdFormulaException {
        if(left != null) left.semanticScan();
        if(right != null) right.semanticScan();
    }

}
