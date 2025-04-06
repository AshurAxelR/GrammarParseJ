package com.xrbpowered.parser.token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TokeniserBuilder<T> {

	protected List<TokeniserRule<T>> rules = new ArrayList<>();

	public TokeniserBuilder<T> rule(Pattern pattern, TokenProvider<T> tokenProvider) {
		rules.add(new TokeniserRule<>(pattern, tokenProvider));
		return this;
	}

	public TokeniserBuilder<T> rule(String regex, int flags, TokenProvider<T> tokenProvider) {
		return rule(Pattern.compile(regex, flags), tokenProvider);
	}

	public TokeniserBuilder<T> rule(String regex, TokenProvider<T> tokenProvider) {
		return rule(Pattern.compile(regex), tokenProvider);
	}

	public Tokeniser<T> build() {
		return new Tokeniser<>(rules);
	}

}
