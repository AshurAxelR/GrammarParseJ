package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;
import java.util.List;

import com.xrbpowered.parser.err.OutputGeneratorException;

public abstract class Function extends Expression {

	public final String name;
	public final List<Expression> args;

	public Function(String name, List<Expression> args) {
		this.name = name;
		this.args = args;
	}

	private static void checkArgs(int expected, List<Expression> args) {
		if(args.size() != expected)
			throw new OutputGeneratorException("expected arguments: " + expected);
	}

	@Override
	public void printAST(PrintStream out) {
		out.printf("%s(", name);
		boolean first = true;
		for(Expression arg : args) {
			if(!first)
				out.print(",");
			first = false;
			arg.printAST(out);
		}
		out.print(")");
	}

	public static Function create(String name, List<Expression> args) {
		switch(name) {
			case "pi":
				checkArgs(0, args);
				return new Function("pi", args) {
					@Override
					public Double calc() {
						return Math.PI;
					}
				};
			case "abs":
				checkArgs(1, args);
				return new Function("abs", args) {
					@Override
					public Double calc() {
						return Math.abs(args.get(0).calc());
					}
				};
			case "sqrt":
				checkArgs(1, args);
				return new Function("sqrt", args) {
					@Override
					public Double calc() {
						return Math.sqrt(args.get(0).calc());
					}
				};
			case "sin":
				checkArgs(1, args);
				return new Function("sin", args) {
					@Override
					public Double calc() {
						return Math.sin(args.get(0).calc());
					}
				};
			case "cos":
				checkArgs(1, args);
				return new Function("cos", args) {
					@Override
					public Double calc() {
						return Math.sin(args.get(0).calc());
					}
				};
			case "deg":
				checkArgs(1, args);
				return new Function("deg", args) {
					@Override
					public Double calc() {
						return Math.toDegrees(args.get(0).calc());
					}
				};
			case "rad":
				checkArgs(1, args);
				return new Function("rad", args) {
					@Override
					public Double calc() {
						return Math.toRadians(args.get(0).calc());
					}
				};
			case "min":
				checkArgs(2, args);
				return new Function("min", args) {
					@Override
					public Double calc() {
						return Math.min(args.get(0).calc(), args.get(1).calc());
					}
				};
			case "max":
				checkArgs(2, args);
				return new Function("max", args) {
					@Override
					public Double calc() {
						return Math.max(args.get(0).calc(), args.get(1).calc());
					}
				};
			case "pow":
				checkArgs(2, args);
				return new Function("pow", args) {
					@Override
					public Double calc() {
						return Math.pow(args.get(0).calc(), args.get(1).calc());
					}
				};
			default:
				throw new OutputGeneratorException("unknown function " + name);
		}
	}

}
