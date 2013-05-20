package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree.*;
import static cz.cuni.lf1.lge.ThunderSTORM.thresholding.FormulaToken.*;

class FormulaParser {

    FormulaLexer lexer;
    FormulaToken token;
    
    public FormulaParser(String formula) {
        lexer = new FormulaLexer(formula);
    }
    
    public int peek() {
        if(token == null) token = lexer.nextToken();
        return token.type;
    }
    
    public String match(int type) throws ThresholdFormulaException {
        if(peek() == type) {
            String tok = token.token;
            token = null;
            return tok;
        }
        error();
        // this will never happen due to thrown ThresholdFormulaException from error()
        assert(true);
        return null;
    }

    public Node parse() throws ThresholdFormulaException {
        return expr();
    }
    
    // ---------------------------------------------- //
    // --- Implementation of LL1 recursive parser --- //
    // ---------------------------------------------- //
    private void error() throws ThresholdFormulaException {
        // Note: if needed, it is possible to add more information
        //       about the error, but I don't think it is necessary
        throw new ThresholdFormulaException("Syntax error!");
    }
    
    private Node expr() throws ThresholdFormulaException {
        return addSubExpr();
    }

    private Node addSubExpr() throws ThresholdFormulaException {    // l+r, l-r
        Node l = mulDivExpr();
        switch(peek()) {
            case OP_ADD: match(OP_ADD); return new Operator(OP_ADD, l, mulDivExpr());
            case OP_SUB: match(OP_SUB); return new Operator(OP_SUB, l, mulDivExpr());
        }
        return l;
    }
    
    private Node mulDivExpr() throws ThresholdFormulaException {    // l*r, l/r
        Node l = powExpr();
        switch(peek()) {
            case OP_MUL: match(OP_MUL); return new Operator(OP_MUL, l, powExpr());
            case OP_DIV: match(OP_DIV); return new Operator(OP_DIV, l, powExpr());
        }
        return l;
    }
    
    private Node powExpr() throws ThresholdFormulaException {   // x^n
        Node x = atom();
        if(peek() == OP_POW) return new Operator(OP_POW, x, atom());
        return x;
    }

    private Node atom() throws ThresholdFormulaException {
        switch(peek()) {
            case LPAR: match(LPAR); Node e = expr(); match(RPAR); return e;
            case FLOAT: return floatVal();
            case NAME: return name();
        }
        error();
        // this will never happen due to thrown ThresholdFormulaException from error()
        assert(true);
        return null;
    }
    
    private Node name() throws ThresholdFormulaException {
        String tok = match(NAME);
        switch(peek()) {
            case DOT: match(DOT); return new Variable(tok, match(NAME));    // object.variable
            case LPAR: match(LPAR); Node arg = expr(); match(RPAR); return new Function(tok, arg); // function call
        }
        return new Variable(tok);   // just a variable (no object)
    }
    
    private Node floatVal() throws ThresholdFormulaException {
        return new Constant(match(FLOAT));
    }
    
}
