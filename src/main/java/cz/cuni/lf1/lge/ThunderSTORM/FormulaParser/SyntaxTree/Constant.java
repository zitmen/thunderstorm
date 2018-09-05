
package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

public class Constant extends Node {

	RetVal val;

	public Constant(String str) {
		val = new RetVal(Double.parseDouble(str));
	}

	@Override
	public RetVal eval(Object param) {
		return val;
	}

	@Override
	public void semanticScan() {
		//
	}

}
