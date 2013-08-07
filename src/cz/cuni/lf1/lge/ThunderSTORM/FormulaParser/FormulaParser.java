package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser;

import static cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.NodeFactory.*;
import static cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaToken.*;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Operator;

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
        error("Syntax error near `" + token.token + "`. Expected `" + FormulaToken.toString(type) + "` instead!");
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
    private void error(String message) throws FormulaParserException {
        if((message == null) || message.trim().isEmpty()) {
            message = "Syntax error!";
        }
        throw new FormulaParserException(message);
    }
    
    private Node expr() throws FormulaParserException {
        return logOrExpr();
    }
    
    private Node logOrExpr() throws FormulaParserException {    // l|r
        return logOrExprTail(logAndExpr());
    }
    
    private Node logOrExprTail(Node l) throws FormulaParserException {    // l|r
        switch(peek()) {
            case OP_OR: match(OP_OR); return logOrExprTail(getNewOperator(Operator.OR, l, logAndExpr()));
        }
        return l;
    }
    
    private Node logAndExpr() throws FormulaParserException {    // l&r
        return logAndExprTail(relExpr());
    }
    
    private Node logAndExprTail(Node l) throws FormulaParserException {    // l&r
        switch(peek()) {
            case OP_AND: match(OP_AND); return logAndExprTail(getNewOperator(Operator.AND, l, relExpr()));
        }
        return l;
    }
    
    private Node relExpr() throws FormulaParserException {    // l=r, l<r, l>r
        return relExprTail(addSubExpr());
    }
    
    private Node relExprTail(Node l) throws FormulaParserException {    // l=r, l<r, l>r
        switch(peek()) {
            case OP_EQ: match(OP_EQ); return relExprTail(getNewOperator(Operator.EQ, l, addSubExpr()));
            case OP_GT: match(OP_GT); return relExprTail(getNewOperator(Operator.GT, l, addSubExpr()));
            case OP_LT: match(OP_LT); return relExprTail(getNewOperator(Operator.LT, l, addSubExpr()));
        }
        return l;
    }

    private Node addSubExpr() throws FormulaParserException {    // l+r, l-r
        return addSubExprTail(mulDivExpr());
    }
    
    private Node addSubExprTail(Node l) throws FormulaParserException {    // l+r, l-r
        switch(peek()) {
            case OP_ADD: match(OP_ADD); return addSubExprTail(getNewOperator(Operator.ADD, l, mulDivExpr()));
            case OP_SUB: match(OP_SUB); return addSubExprTail(getNewOperator(Operator.SUB, l, mulDivExpr()));
        }
        return l;
    }
    
    private Node mulDivExpr() throws FormulaParserException {    // l*r, l/r, l%r
        return mulDivExprTail(powExpr());
    }
    
    private Node mulDivExprTail(Node l) throws FormulaParserException {    // l*r, l/r, l%r
        switch(peek()) {
            case OP_MUL: match(OP_MUL); return mulDivExprTail(getNewOperator(Operator.MUL, l, powExpr()));
            case OP_DIV: match(OP_DIV); return mulDivExprTail(getNewOperator(Operator.DIV, l, powExpr()));
            case OP_MOD: match(OP_MOD); return mulDivExprTail(getNewOperator(Operator.MOD, l, powExpr()));
        }
        return l;
    }
    
    private Node powExpr() throws FormulaParserException {   // x^n
        return powExprTail(unaryExpr());
    }
    
    private Node powExprTail(Node l) throws FormulaParserException {   // x^n
        if(peek() == OP_POW) {
            match(OP_POW);
            return powExprTail(getNewOperator(Operator.POW, l, unaryExpr()));
        }
        return l;
    }
    
    private Node unaryExpr() throws FormulaParserException {   // -x or +x
        switch(peek()) {
            case OP_ADD: match(OP_ADD); return getNewOperator(Operator.ADD, getNewConstant("0"), atom());
            case OP_SUB: match(OP_SUB); return getNewOperator(Operator.SUB, getNewConstant("0"), atom());
        }
        return atom();
    }

    private Node atom() throws FormulaParserException {
        switch(peek()) {
            case LPAR: match(LPAR); Node e = expr(); match(RPAR); return e;
            case FLOAT: return floatVal();
            case NAME: return name();
        }
        error("Syntax error near `" + token.token + "`. Expected `(expression)` or a number or a variable instead!");
        // the following will never happen due to the FormulaParserException thrown from the error()
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
