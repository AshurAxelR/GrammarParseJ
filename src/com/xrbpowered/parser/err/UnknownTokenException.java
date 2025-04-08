package com.xrbpowered.parser.err;

public class UnknownTokenException extends ParserException {

	public UnknownTokenException(int pos, char ch) {
		super(pos, "unrecognised token "+ch);
	}

}
