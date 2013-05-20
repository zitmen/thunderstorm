package cz.cuni.lf1.lge.ThunderSTORM.thresholding;

import java.util.Scanner;

class FormulaLexer {
    
    Scanner scanner;

    FormulaLexer(String formula) {
        scanner = new Scanner(formula.trim());
        scanner.useDelimiter("");
    }

    FormulaToken nextToken() {
        if(!scanner.hasNext()) return new FormulaToken(FormulaToken.EOI);
        scanner.skip("[ ]*");
        
        FormulaToken token = new FormulaToken();
        if(scanner.hasNextFloat()) {
            token.type = FormulaToken.FLOAT;
            token.token = Float.toString(scanner.nextFloat());
        } else if(scanner.hasNext("[a-zA-Z]")) {
            token.type = FormulaToken.NAME;
            token.token = scanner.findInLine("[a-zA-Z0-9]+");
        } else {
            token.token = scanner.next();    // --> nextCharacter()
            switch(token.token.charAt(0)) {
                case '+': token.type = FormulaToken.OP_ADD; break;
                case '-': token.type = FormulaToken.OP_SUB; break;
                case '*': token.type = FormulaToken.OP_MUL; break;
                case '/': token.type = FormulaToken.OP_DIV; break;
                case '^': token.type = FormulaToken.OP_POW; break;
                case '(': token.type = FormulaToken.LPAR; break;
                case ')': token.type = FormulaToken.RPAR; break;
                case '.': token.type = FormulaToken.DOT; break;
                default : token.type = FormulaToken.UNKNOWN; break;
            }
        }
        return token;
    }

}
