package com.xrbpowered.parser.token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokeniserRule<T> {
	public final Pattern pattern;
	public final TokenProviderEx<T> tokenProvider;
	
	private Matcher matcher;
	
	public TokeniserRule(Pattern pattern, TokenProviderEx<T> tokenProvider) {
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
	
	public T getToken() {
		if(tokenProvider==null)
			return null;
		else
			return tokenProvider.getToken(matcher);
	}
}