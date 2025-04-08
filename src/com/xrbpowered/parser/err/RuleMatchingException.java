package com.xrbpowered.parser.err;

import com.xrbpowered.parser.grammar.GrammarParser;

public class RuleMatchingException extends ParserException {

	public RuleMatchingException(GrammarParser parser, String msg) {
		super(parser.getPos(), msg);
	}

}
