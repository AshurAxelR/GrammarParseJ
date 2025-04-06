package com.xrbpowered.parser.examples.calc.ast;

import java.util.List;

public abstract class Function extends Expression {

	public final List<Expression> args;
	
	public Function(List<Expression> args) {
		this.args = args;
	}

	private static void checkArgs(int expected, List<Expression> args) {
		if(args.size()!=expected)
			throw new RuntimeException("expected arguments: " + expected);
	}
	
	public static Function create(String name, List<Expression> args) {
		switch(name) {
			case "pi":
				checkArgs(0, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.PI;
					}
				};
			case "sqrt":
				checkArgs(1, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.sqrt(args.get(0).calc());
					}
				};
			case "sin":
				checkArgs(1, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.sin(args.get(0).calc());
					}
				};
			case "cos":
				checkArgs(1, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.sin(args.get(0).calc());
					}
				};
			case "deg":
				checkArgs(1, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.toDegrees(args.get(0).calc());
					}
				};
			case "rad":
				checkArgs(1, args);
				return new Function(args) {
					@Override
					public Double calc() {
						return Math.toRadians(args.get(0).calc());
					}
				};
			default:
				throw new RuntimeException("unknown function " + name);
		}
	}
	
}
