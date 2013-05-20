package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

public class Constant extends Node {

    RetVal val;

    public Constant(String str) {
        val = new RetVal(Float.parseFloat(str));
    }
    
    @Override
    public RetVal eval() {
        return val;
    }

    @Override
    public void semanticScan() {
        //
    }

}
