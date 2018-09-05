
package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;

public class NodeFactory {

	private static int nodeType = -1;

	public static void setFormulaType(int type) {
		switch (type) {
			case FormulaParser.FORMULA_THRESHOLD:
				nodeType = Node.THRESHOLDING;
				break;
			case FormulaParser.FORMULA_RESULTS_FILTER:
				nodeType = Node.RESULTS_FILTERING;
				break;
			default:
				nodeType = -1;
				break;
		}
	}

	public static Variable getNewVariable(String objName, String varName) {
		Variable var = new Variable(objName, varName);
		var.setNodeType(nodeType);
		return var;
	}

	public static Variable getNewVariable(String varName) {
		Variable var = new Variable(null, varName);
		var.setNodeType(nodeType);
		return var;
	}

	public static Operator getNewOperator(int operator, Node left, Node right) {
		Operator op = new Operator(operator, left, right);
		op.setNodeType(nodeType);
		return op;
	}

	public static Function getNewFunction(String fnName, Node argument) {
		Function fn = new Function(fnName, argument);
		fn.setNodeType(nodeType);
		return fn;
	}

	public static Constant getNewConstant(String value) {
		Constant c = new Constant(value);
		c.setNodeType(nodeType);
		return c;
	}

}
