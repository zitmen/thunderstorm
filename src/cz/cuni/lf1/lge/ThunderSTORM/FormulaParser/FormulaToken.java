package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser;

class FormulaToken {
    
    public static final int UNKNOWN = -1;
    public static final int EOI = 0;    // End Of Input
    public static final int OP_ADD = 1;
    public static final int OP_SUB = 2;
    public static final int OP_MUL = 3;
    public static final int OP_DIV = 4;
    public static final int OP_MOD = 5;
    public static final int OP_POW = 6;
    public static final int OP_AND = 7;
    public static final int OP_OR = 8;
    public static final int OP_LT = 9;
    public static final int OP_GT = 10;
    public static final int OP_EQ = 11;
    public static final int LPAR = 12;
    public static final int RPAR = 13;
    public static final int DOT = 14;
    public static final int NAME = 15;
    public static final int FLOAT = 16;
    
    public int type;
    public String token;

    public FormulaToken() {
        this.type = UNKNOWN;
    }
    
    public FormulaToken(int type) {
        this.type = EOI;
    }
    
    public FormulaToken(int type, String token) {
        this.type = type;
        this.token = token;
    }
    
}
