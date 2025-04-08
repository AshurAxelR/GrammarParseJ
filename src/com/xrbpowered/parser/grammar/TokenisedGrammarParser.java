package com.xrbpowered.parser.grammar;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.token.Tokeniser;

public abstract class TokenisedGrammarParser<T> extends GrammarParser {

	protected final Tokeniser<T> tokeniser;
	protected T token = null;
	
	public TokenisedGrammarParser(Tokeniser<T> tokeniser) {
		this.tokeniser = tokeniser;
	}
	
	public void debugTokenPos(String msg) {
		System.out.printf("(%s) token=%s, tokenIndex=%d, index=%d, end=%d\n",
				msg, token==null ? "null" : token.toString(),
				tokeniser.getTokenIndex(), tokeniser.getIndex(), tokeniser.getEnd());
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
	protected void restorePos(int index) {
		if(index!=tokeniser.getTokenIndex()) {
			tokeniser.jumpTo(index);
			try {
				next();
			}
			catch(ParserException e) {
				// never happens
			}
		}
	}

}
