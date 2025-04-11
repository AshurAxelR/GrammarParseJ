package com.xrbpowered.parser.err;

import com.xrbpowered.parser.grammar.GrammarParser;

public class UnexpectedTokenException extends RuleMatchingException {

	public UnexpectedTokenException(GrammarParser parser, String fmt) {
		super(parser, String.format(fmt, parser.tokenName()));
	}

	public UnexpectedTokenException(GrammarParser parser) {
		this(parser, "unexpected %s");
	}

}