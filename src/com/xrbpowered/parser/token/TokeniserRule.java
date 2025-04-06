package com.xrbpowered.parser.token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokeniserRule<T> {
	public final Pattern pattern;
	public final TokenProvider<T> tokenProvider;
	
	private Matcher matcher;
	
	public TokeniserRule(Pattern pattern, TokenProvider<T> tokenProvider) {
		this.pattern = pattern;
		this.tokenProvider = tokenProvider;
	}
	
	public void setSource(String source) {
		if(matcher==null)
			matcher = pattern.matcher(source);
		else
			matcher.reset(source);
	}
	
	public Matcher getMatcher() {
		return matcher;
	}
	
	public void freeMatcher() {
		matcher = null;
	}
	
	public T getToken(String raw) {
		if(tokenProvider==null)
			return null;
		else
			return tokenProvider.getToken(raw);
	}
}