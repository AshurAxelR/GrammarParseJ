package com.xrbpowered.parser.token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TokeniserBuilder<T> {

	protected List<TokeniserRule<T>> rules = new ArrayList<>();
	protected int flags;
	
	public TokeniserBuilder(int flags) {
		this.flags = flags;
	}

	public TokeniserBuilder() {
		this(0);
	}

	public TokeniserBuilder<T> ruleEx(Pattern pattern, TokenProviderEx<T> tokenProvider) {
		rules.add(new TokeniserRule<>(pattern, tokenProvider));
		return this;
	}

	public TokeniserBuilder<T> ruleEx(String regex, TokenProviderEx<T> tokenProvider) {
		return ruleEx(Pattern.compile(regex, flags), tokenProvider);
	}

	public TokeniserBuilder<T> rule(Pattern pattern, int group, TokenProvider<T> tokenProvider) {
		return ruleEx(pattern, TokenProviderEx.group(group, tokenProvider));
	}

	public TokeniserBuilder<T> rule(String regex, int group, TokenProvider<T> tokenProvider) {
		return rule(Pattern.compile(regex, flags), group, tokenProvider);
	}
	
	public TokeniserBuilder<T> rule(Pattern pattern, TokenProvider<T> tokenProvider) {
		return ruleEx(pattern, TokenProviderEx.all(tokenProvider));
	}

	public TokeniserBuilder<T> rule(String regex, TokenProvider<T> tokenProvider) {
		return rule(Pattern.compile(regex, flags), tokenProvider);
	}

	public Tokeniser<T> build() {
		return new Tokeniser<>(rules);
	}

}
