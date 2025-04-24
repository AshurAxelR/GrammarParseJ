package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;

public class ConstValue extends Expression {

	public final double x;

	public ConstValue(double x) {
		this.x = x;
	}

	@Override
	public Double calc() {
		return x;
	}

	@Override
	public void printAST(PrintStream out) {
		out.print(x);
	}

}
