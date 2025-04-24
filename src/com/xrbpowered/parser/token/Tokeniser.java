package com.xrbpowered.parser.token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import com.xrbpowered.parser.err.TokenProviderException;
import com.xrbpowered.parser.err.TokeniserException;
import com.xrbpowered.parser.err.UnknownTokenException;

public class Tokeniser<T> {	
	
	protected final List<TokeniserRule<T>> rules;

	protected String source = null;
	protected int end = 0;
	protected int pos = 0;
	protected int tokenPos = 0;
	
	public Tokeniser(Collection<TokeniserRule<T>> rules) {
		this.rules = List.copyOf(rules);
	}
	
	public void start(File file) throws FileNotFoundException, IOException {
		try(FileInputStream f = new FileInputStream(file)) {
			start(new String(f.readAllBytes(), StandardCharsets.UTF_8));
		}
	}
	
	public void start(String source, int start, int end) {
		this.source = source;
		this.pos = start;
		this.end = end;
		for(TokeniserRule<T> rule : rules)
			rule.setSource(source);
	}

	public void start(String source, int start) {
		start(source, start, source.length());
	}

	public void start(String source) {
		start(source, 0);
	}
	
	public String getSource() {
		return source;
	}
	
	public int getEnd() {
		return end;
	}

	public int getPos() {
		return pos;
	}

	public int getTokenPos() {
		return tokenPos;
	}
	
	public void jumpTo(int pos) {
		this.pos = pos;
	}
	
	public T getNextToken(boolean skipVoid) throws TokeniserException {
		while(pos<end) {
			TokeniserRule<T> match = null;
			for(TokeniserRule<T> rule : rules) {
				Matcher m = rule.getMatcher();
				m.region(pos, end);
				if(m.lookingAt()) {
					match = rule;
					break;
				}
			}
			
			if(match!=null) {
				tokenPos = pos;
				try {
					T t = match.getToken();
					jumpTo(match.getMatcher().end());
					if(t!=null || !skipVoid)
						return t;
				}
				catch(NumberFormatException ex) {
					throw(new TokeniserException(pos, "bad number format", ex));
				}
				catch(TokenProviderException ex) {
					throw(new TokeniserException(pos, ex.getMessage(), ex));
				}
			}
			else
				throw(new UnknownTokenException(pos, source.charAt(pos)));
		}
		tokenPos = end;
		return null;
	}
	
	public T getNextToken() throws TokeniserException {
		return getNextToken(true);
	}

	public static String convertString(String s, Tokeniser<String> t) throws TokeniserException {
		t.start(s);
		StringBuilder out = new StringBuilder();
		for(;;) {
			String c = t.getNextToken();
			if(c==null)
				return out.toString();
			else
				out.append(c);
		}
	}
}
