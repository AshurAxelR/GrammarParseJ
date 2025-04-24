package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;

public abstract class Expression {

	public abstract Double calc();
	public abstract void printAST(PrintStream out);

}
