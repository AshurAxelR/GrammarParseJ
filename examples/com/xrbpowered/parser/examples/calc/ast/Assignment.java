package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;
import java.util.Map;

public class Assignment {

	public final String varName;
	public final Expression x;

	public Assignment(String varName, Expression x) {
		this.varName = varName == null ? "result" : varName;
		this.x = x;
	}

	public void exec(Map<String, Double> vars, PrintStream out) {
		Double val = x.calc();
		vars.put(varName, val);
		if(out != null)
			out.printf("%s = %s\n", varName, val.toString());
	}

	public void printAST(PrintStream out) {
		out.printf("%s = ", varName);
		x.printAST(out);
		out.print("\n");
	}

}
