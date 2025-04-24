package com.xrbpowered.parser.examples.calc.ast;

import java.io.PrintStream;

import com.xrbpowered.parser.err.OutputGeneratorException;

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

	public static BinaryOp select(char op, Expression x, Expression y) {
		return switch(op) {
			case '+' -> add(x, y);
			case '-' -> sub(x, y);
			case '*' -> mul(x, y);
			case '/' -> div(x, y);
			default -> throw new OutputGeneratorException("unknown operator " + op);
		};
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
