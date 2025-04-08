package com.xrbpowered.parser.err;

public class ParserException extends Exception {

	public final int pos;
	
	public ParserException(int pos, String msg, Throwable cause) {
		super(msg, cause);
		this.pos = pos;
	}

	public ParserException(int pos, String msg) {
		this(pos, msg, null);
	}

	public ParserException(int pos, Throwable cause) {
		this(pos, cause.getMessage(), cause);
	}
}
