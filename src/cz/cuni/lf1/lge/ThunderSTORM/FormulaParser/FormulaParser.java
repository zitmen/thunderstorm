package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser;

import static cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.NodeFactory.*;
import static cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaToken.*;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Operator;
// TODO: implementovat relacni a logicke operatory !!
public class FormulaParser {

    public final static int FORMULA_THRESHOLD = 1;
    public final static int FORMULA_RESULTS_FILTER = 2;
    
    private FormulaLexer lexer;
    private FormulaToken token;
    
    public FormulaParser(String formula, int formulaType) {
      setFormulaType(formulaType);
      lexer = new FormulaLexer(formula);
    }
    
    private int peek() {
        if(token == null) token = lexer.nextToken();
        return token.type;
    }
    
    private String match(int type) throws FormulaParserException {
        if(peek() == type) {
            String tok = token.token;
            token = null;
            return tok;
        }
        error();
        // this will never happen due to thrown FormulaParserException from error()
        assert(true);
        return null;
    }

    public Node parse() throws FormulaParserException {
        return expr();
    }
    
    // ---------------------------------------------- //
    // --- Implementation of LL1 recursive parser --- //
    // ---------------------------------------------- //
    private void error() throws FormulaParserException {
        // Note: if needed, it is possible to add more information
        //       about the error, but I don't think it is necessary
        throw new FormulaParserException("Syntax error!");
    }
    
    private Node expr() throws FormulaParserException {
        return logOrExpr();
    }
    
    private Node logOrExpr() throws FormulaParserException {    // l|r
        Node l = logAndExpr();
        switch(peek()) {
            case OP_OR: match(OP_OR); return getNewOperator(Operator.OR, l, logAndExpr());
        }
        return l;
    }
    
    private Node logAndExpr() throws FormulaParserException {    // l&r
        Node l = relExpr();
        switch(peek()) {
            case OP_AND: match(OP_AND); return getNewOperator(Operator.AND, l, relExpr());
        }
        return l;
    }
    
    private Node relExpr() throws FormulaParserException {    // l=r, l<r, l>r
        Node l = addSubExpr();
        switch(peek()) {
            case OP_EQ: match(OP_EQ); return getNewOperator(Operator.EQ, l, addSubExpr());
            case OP_GT: match(OP_GT); return getNewOperator(Operator.GT, l, addSubExpr());
            case OP_LT: match(OP_LT); return getNewOperator(Operator.LT, l, addSubExpr());
        }
        return l;
    }

    private Node addSubExpr() throws FormulaParserException {    // l+r, l-r
        Node l = mulDivExpr();
        switch(peek()) {
            case OP_ADD: match(OP_ADD); return getNewOperator(Operator.ADD, l, mulDivExpr());
            case OP_SUB: match(OP_SUB); return getNewOperator(Operator.SUB, l, mulDivExpr());
        }
        return l;
    }
    
    private Node mulDivExpr() throws FormulaParserException {    // l*r, l/r, l%r
        Node l = powExpr();
        switch(peek()) {
            case OP_MUL: match(OP_MUL); return getNewOperator(Operator.MUL, l, powExpr());
            case OP_DIV: match(OP_DIV); return getNewOperator(Operator.DIV, l, powExpr());
            case OP_MOD: match(OP_MOD); return getNewOperator(Operator.MOD, l, powExpr());
        }
        return l;
    }
    
    private Node powExpr() throws FormulaParserException {   // x^n
        Node x = atom();
        if(peek() == OP_POW) return getNewOperator(Operator.POW, x, atom());
        return x;
    }

    private Node atom() throws FormulaParserException {
        switch(peek()) {
            case LPAR: match(LPAR); Node e = expr(); match(RPAR); return e;
            case FLOAT: return floatVal();
            case NAME: return name();
        }
        error();
        // this will never happen due to thrown FormulaParserException from error()
        assert(true);
        return null;
    }
    
    private Node name() throws FormulaParserException {
        String tok = match(NAME);
        switch(peek()) {
            case DOT: match(DOT); return getNewVariable(tok, match(NAME));    // object.variable
            case LPAR: match(LPAR); Node arg = expr(); match(RPAR); return getNewFunction(tok, arg); // function call
        }
        return getNewVariable(tok);   // just a variable (no object)
    }
    
    private Node floatVal() throws FormulaParserException {
        return getNewConstant(match(FLOAT));
    }
    
}
