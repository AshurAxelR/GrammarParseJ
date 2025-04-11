package com.xrbpowered.parser.examples.tokens;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.examples.json.JsonExample;
import com.xrbpowered.parser.examples.json.JsonParser;
import com.xrbpowered.parser.token.LineColProvider;
import com.xrbpowered.parser.token.LineColProvider.LineColPos;

public class ListTokensExample extends JsonParser {

	public class TokenPos {
		public final Token token;
		public final int pos;
		public TokenPos() {
			this.token = ListTokensExample.this.token;
			this.pos = getPos();
		}
		@Override
		public String toString() {
			return String.format("%s %s value=%s",
					formatPos(pos), tokenName(token), tokenValue(token));
		}
	}
	
	private LineColProvider lc;
	
	private String formatPos(int pos) {
		LineColPos lcPos = lc.find(pos, true);
		return String.format("[%d] (%d:%d)", lcPos.pos(), lcPos.line()+1, lcPos.col()+1);
	}
	
	private Deque<TokenPos> listTokens() {
		lc = new LineColProvider(tokeniser.getSource(), 4);
		Deque<TokenPos> list = new LinkedList<>();
		try {
			next();
			while(!isEnd()) {
				list.add(new TokenPos());
				next();
			}
		}
		catch(ParserException ex) {
			System.err.printf("%s %s\n", formatPos(ex.pos), ex.getMessage());
		}
		return list;
	}
	
	public Deque<TokenPos> listTokens(File file) {
		try {
			tokeniser.start(file);
			return listTokens();
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
			return new LinkedList<>();
		}
	}

	public Deque<TokenPos> listTokens(String s) {
		tokeniser.start(s);
		return listTokens();
	}
	
	public static void main(String[] args) {
		Deque<TokenPos> list = new ListTokensExample().listTokens(new File(JsonExample.EXAMPLE_PATH));
		for(TokenPos t : list)
			System.out.println(t);
		
		// print in reverse
		// while(!list.isEmpty())
		//	System.out.println(list.removeLast());
	}

}
