package com.xrbpowered.parser.examples.calc.ast;

public abstract class UnaryOp extends Expression {

	public final Expression x;
	
	public UnaryOp(Expression x) {
		this.x = x;
	}
	
	public static UnaryOp neg(Expression x) {
		return new UnaryOp(x) {
			@Override
			public Double calc() {
				return -x.calc();
			}
		};
	}

}
