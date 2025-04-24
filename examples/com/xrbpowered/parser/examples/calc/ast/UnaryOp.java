package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;

public abstract class UnaryOp extends Expression {

	public final String op;
	public final Expression x;

	public UnaryOp(String op, Expression x) {
		this.op = op;
		this.x = x;
	}

	@Override
	public void printAST(PrintStream out) {
		out.printf("(%s", op);
		x.printAST(out);
		out.print(")");
	}

	public static UnaryOp neg(Expression x) {
		return new UnaryOp("-", x) {
			@Override
			public Double calc() {
				return -x.calc();
			}
		};
	}

}
