
package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;

public class Operator extends Node {

	public static final int ADD = 1;
	public static final int SUB = 2;
	public static final int MUL = 3;
	public static final int DIV = 4;
	public static final int MOD = 5;
	public static final int POW = 6;

	// the following operators are not available for thresholding!! see
	// checkSemantics()!
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
	public RetVal eval(Object param) {
		switch (op) {
			case ADD:
				return left.eval(param).add(right.eval(param));
			case SUB:
				return left.eval(param).sub(right.eval(param));
			case MUL:
				return left.eval(param).mul(right.eval(param));
			case DIV:
				return left.eval(param).div(right.eval(param));
			case MOD:
				return left.eval(param).mod(right.eval(param));
			case POW:
				return left.eval(param).pow(right.eval(param));
			case AND:
				return left.eval(param).and(right.eval(param));
			case OR:
				return left.eval(param).or(right.eval(param));
			case LT:
				return left.eval(param).lt(right.eval(param));
			case GT:
				return left.eval(param).gt(right.eval(param));
			case EQ:
				return left.eval(param).eq(right.eval(param));
			case NEQ:
				return left.eval(param).neq(right.eval(param));
			default:
				throw new UnsupportedOperationException(
					"Unknown operator! Only supported operators are: + - * / ^ & | < > = !=");
		}
	}

	@Override
	public void semanticScan() throws FormulaParserException {
		if (isThresholding() && (op > LOGIC_OPERATORS)) {
			throw new FormulaParserException(
				"Illegal operator used! The only allowed operators in thresholding formula are: +, -, *, /, and ^.");
		}
		if (left != null) left.semanticScan();
		if (right != null) right.semanticScan();
	}

}
