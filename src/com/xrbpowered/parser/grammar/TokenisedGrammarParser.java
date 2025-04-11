package com.xrbpowered.parser.grammar;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.token.Tokeniser;

public abstract class TokenisedGrammarParser<T> extends GrammarParser {

	protected final Tokeniser<T> tokeniser;
	protected T token = null;
	
	public TokenisedGrammarParser(Tokeniser<T> tokeniser) {
		this.tokeniser = tokeniser;
	}
	
	@Override
	public int getPos() {
		return tokeniser.getTokenIndex();
	}
	
	@Override
	public boolean isEnd() {
		return token==null;
	}
	
	@Override
	protected void next() throws ParserException {
		token = tokeniser.getNextToken();
	}
	
	@Override
	protected void restorePos(int index) throws ParserException {
		if(index!=tokeniser.getTokenIndex()) {
			tokeniser.jumpTo(index);
			next();
		}
	}

	public abstract Object tokenValue(T token);
	
	public String tokenName(T token) {
		return String.format("token: %s", tokenValue(token));
	}

	@Override
	public final Object tokenValue() {
		return tokenValue(token);
	}
	
	@Override
	public final String tokenName() {
		return tokenName(token);
	}

}
