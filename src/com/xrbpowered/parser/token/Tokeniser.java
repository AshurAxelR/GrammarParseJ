package com.xrbpowered.parser.token;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import com.xrbpowered.parser.err.UnknownTokenException;

public class Tokeniser<T> {	
	
	protected final List<TokeniserRule<T>> rules;

	protected String source = null;
	protected int end = 0;
	protected int index = 0;
	protected int tokenIndex = 0;
	
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
		this.index = start;
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

	public int getIndex() {
		return index;
	}

	public int getTokenIndex() {
		return tokenIndex;
	}
	
	public void jumpTo(int index) {
		this.index = index;
	}
	
	public T getNextToken(boolean skipVoid) throws UnknownTokenException {
		while(index<end) {
			TokeniserRule<T> match = null;
			for(TokeniserRule<T> rule : rules) {
				Matcher m = rule.getMatcher();
				m.region(index, end);
				if(m.lookingAt()) {
					match = rule;
					break;
				}
			}
			
			if(match!=null) {
				tokenIndex = index;
				String raw = match.getMatcher().group();
				T t = match.getToken(raw);
				jumpTo(match.getMatcher().end());
				if(t!=null || !skipVoid)
					return t;
			}
			else
				throw(new UnknownTokenException(index, source.charAt(index)));
		}
		tokenIndex = end;
		return null;
	}
	
	public T getNextToken() throws UnknownTokenException {
		return getNextToken(true);
	}

}
