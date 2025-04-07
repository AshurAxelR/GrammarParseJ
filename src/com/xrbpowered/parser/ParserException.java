package com.xrbpowered.parser;

public class ParserException extends Exception {

	public final int pos;
	public final int line;
	
	public ParserException(int pos, int line, String msg, Throwable cause) {
		super(String.format("(%d) %s", line, msg), cause);
		this.pos = pos;
		this.line = line;
	}

	public ParserException(int pos, int line, String msg) {
		this(pos, line, msg, null);
	}

	public ParserException(int pos, int line, Throwable cause) {
		this(pos, line, cause.getMessage(), cause);
	}
}
