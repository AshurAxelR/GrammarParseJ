package com.xrbpowered.parser.examples.calc.ast;

public class ConstValue extends Expression {

	public final double x;
	
	public ConstValue(double x) {
		this.x = x;
	}
	
	@Override
	public Double calc() {
		return x;
	}
	
}
