package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser;

import java.util.Scanner;

class FormulaLexer {
    
    Scanner scanner;

    public FormulaLexer(String formula) {
        scanner = new Scanner(formula.trim());
        scanner.useDelimiter("");
    }

    public FormulaToken nextToken() {
        if(!scanner.hasNext()) return new FormulaToken(FormulaToken.EOI);
        scanner.skip("[ ]*");
        
        FormulaToken token = new FormulaToken();
        if(scanner.hasNextFloat()) {
            token.type = FormulaToken.FLOAT;
            // note: don't know why scanner.nextFloat does not work??!! using regexp instead:
            //http://stackoverflow.com/questions/2293780/how-to-detect-a-floating-point-number-using-a-regular-expression
            token.token = scanner.findInLine("(([1-9][0-9]*\\.?[0-9]*)|(\\.[0-9]+))([Ee][+-]?[0-9]+)?");
        } else if(scanner.hasNext("[#_a-zA-Z]")) {
            token.type = FormulaToken.NAME;
            token.token = scanner.findInLine("[#_a-zA-Z0-9]+");
        } else {
            token.token = scanner.next();    // --> nextCharacter()
            switch(token.token.charAt(0)) {
                case '+': token.type = FormulaToken.OP_ADD; break;
                case '-': token.type = FormulaToken.OP_SUB; break;
                case '*': token.type = FormulaToken.OP_MUL; break;
                case '/': token.type = FormulaToken.OP_DIV; break;
                case '^': token.type = FormulaToken.OP_POW; break;
                case '&': token.type = FormulaToken.OP_AND; break;
                case '|': token.type = FormulaToken.OP_OR; break;
                case '<': token.type = FormulaToken.OP_LT; break;
                case '>': token.type = FormulaToken.OP_GT; break;
                case '=': token.type = FormulaToken.OP_EQ; break;
                case '(': token.type = FormulaToken.LPAR; break;
                case ')': token.type = FormulaToken.RPAR; break;
                case '.': token.type = FormulaToken.DOT; break;
                default : token.type = FormulaToken.UNKNOWN; break;
            }
        }
        return token;
    }

}
