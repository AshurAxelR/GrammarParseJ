package com.xrbpowered.parser.token;

import com.xrbpowered.parser.ParserException;

public class UnknownTokenException extends ParserException {

	public UnknownTokenException(int pos, int line) {
		super(pos, line, "unrecognised token");
	}

}
