package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;

public class Operator extends Node {
    
    public static final int ADD = 1;
    public static final int SUB = 2;
    public static final int MUL = 3;
    public static final int DIV = 4;
    public static final int MOD = 5;
    public static final int POW = 6;
    
    // the following operators are not available for thresholding!! see checkSemantics()!
    public static final int LOGIC_OPERATORS = 10;
    public static final int AND = 11;
    public static final int OR = 12;
    public static final int LT = 13;
    public static final int GT = 14;
    public static final int EQ = 15;
    public static final int NEQ = 16;
    
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
            case MOD: return left.eval().mod(right.eval());
            case POW: return left.eval().pow(right.eval());
            case AND: return left.eval().and(right.eval());
            case OR : return left.eval().or (right.eval());
            case LT : return left.eval().lt (right.eval());
            case GT : return left.eval().gt (right.eval());
            case EQ : return left.eval().eq (right.eval());
            case NEQ: return left.eval().neq(right.eval());
            default : throw new UnsupportedOperationException("Unknown operator! Only supported operators are: + - * / ^ & | < > = !=");
        }
    }

    @Override
    public void semanticScan() throws FormulaParserException {
        if(isThresholding() && (op > LOGIC_OPERATORS)) {
            throw new FormulaParserException("Illegal operator used! The only allowed operators in thresholding formula are: +, -, *, /, and ^.");
        }
        if(left != null) left.semanticScan();
        if(right != null) right.semanticScan();
    }

}
