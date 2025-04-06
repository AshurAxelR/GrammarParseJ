package com.xrbpowered.parser.examples.calc.ast;

public abstract class BinaryOp extends Expression {

	public final Expression x;
	public final Expression y;

	public BinaryOp(Expression x, Expression y) {
		this.x = x;
		this.y = y;
	}
	
	public static BinaryOp add(Expression x, Expression y) {
		return new BinaryOp(x, y) {
			@Override
			public Double calc() {
				return x.calc() + y.calc();
			}
		};
	}

	public static BinaryOp sub(Expression x, Expression y) {
		return new BinaryOp(x, y) {
			@Override
			public Double calc() {
				return x.calc() - y.calc();
			}
		};
	}

	public static BinaryOp mul(Expression x, Expression y) {
		return new BinaryOp(x, y) {
			@Override
			public Double calc() {
				return x.calc() * y.calc();
			}
		};
	}

	public static BinaryOp div(Expression x, Expression y) {
		return new BinaryOp(x, y) {
			@Override
			public Double calc() {
				return x.calc() / y.calc();
			}
		};
	}

}
