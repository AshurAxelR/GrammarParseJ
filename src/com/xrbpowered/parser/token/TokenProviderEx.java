package com.xrbpowered.parser.token;

import java.util.regex.Matcher;

public interface TokenProviderEx<T> {
	public T getToken(Matcher m);
	
	public static <T> TokenProviderEx<T> all(TokenProvider<T> tokenProvider) {
		return (tokenProvider==null) ? null :
			new TokenProviderEx<T>() {
				@Override
				public T getToken(Matcher m) {
					return tokenProvider.getToken(m.group());
				}
			};
	}

	public static <T> TokenProviderEx<T> group(int g, TokenProvider<T> tokenProvider) {
		return (tokenProvider==null) ? null :
			new TokenProviderEx<T>() {
				@Override
				public T getToken(Matcher m) {
					return tokenProvider.getToken(m.group(g));
				}
			};
	}
}