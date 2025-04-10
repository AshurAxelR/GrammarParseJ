package com.xrbpowered.parser.grammar;

import com.xrbpowered.parser.err.ParserException;

public abstract class ParserRule {

	public final String name;

	public ParserRule(String name) {
		this.name = name;
	}
	
	public abstract void linkRules(GrammarParser parser);
	protected abstract Object lookingAt(boolean top, GrammarParser parser) throws ParserException;

}
