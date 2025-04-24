package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;

public abstract class BinaryOp extends Expression {

	public final String op;
	public final Expression x;
	public final Expression y;

	public BinaryOp(String op, Expression x, Expression y) {
		this.op = op;
		this.x = x;
		this.y = y;
	}

	@Override
	public void printAST(PrintStream out) {
		out.print("(");
		x.printAST(out);
		out.print(op);
		y.printAST(out);
		out.print(")");
	}

	public static BinaryOp add(Expression x, Expression y) {
		return new BinaryOp("+", x, y) {
			@Override
			public Double calc() {
				return x.calc() + y.calc();
			}
		};
	}

	public static BinaryOp sub(Expression x, Expression y) {
		return new BinaryOp("-", x, y) {
			@Override
			public Double calc() {
				return x.calc() - y.calc();
			}
		};
	}

	public static BinaryOp mul(Expression x, Expression y) {
		return new BinaryOp("*", x, y) {
			@Override
			public Double calc() {
				return x.calc() * y.calc();
			}
		};
	}

	public static BinaryOp div(Expression x, Expression y) {
		return new BinaryOp("/", x, y) {
			@Override
			public Double calc() {
				return x.calc() / y.calc();
			}
		};
	}

}
