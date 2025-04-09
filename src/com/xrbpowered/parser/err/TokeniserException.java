package com.xrbpowered.parser.err;

public class TokeniserException extends ParserException {

	public TokeniserException(int pos, String msg) {
		super(pos, msg);
	}

	public TokeniserException(int pos, String msg, Throwable cause) {
		super(pos, msg, cause);
	}

}
