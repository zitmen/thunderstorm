package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;

public class Operator extends Node {
    
    public static final int ADD = 1;
    public static final int SUB = 2;
    public static final int MUL = 3;
    public static final int DIV = 4;
    public static final int POW = 5;
    // the following operators are not available for thresholding!! see checkSemantics()!
    public static final int AND = 6;
    public static final int OR = 7;
    public static final int LT = 8;
    public static final int GT = 9;
    public static final int EQ = 10;
    
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
            case AND: return left.eval().and(right.eval());
            case OR: return left.eval().or(right.eval());
            case LT: return left.eval().lt(right.eval());
            case GT: return left.eval().gt(right.eval());
            case EQ: return left.eval().eq(right.eval());
            default : throw new UnsupportedOperationException("Unknown operator! Only supported operators are: + - * / ^");
        }
    }

    @Override
    public void semanticScan() throws FormulaParserException {
        if(isThresholding() && (op > 5)) {
            throw new FormulaParserException("Illegal operator used! The only allowed operators in thresholding formula are: +, -, *, /, and ^.");
        }
        if(left != null) left.semanticScan();
        if(right != null) right.semanticScan();
    }

}
