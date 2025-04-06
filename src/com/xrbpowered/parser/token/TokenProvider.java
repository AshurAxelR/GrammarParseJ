package com.xrbpowered.parser.token;

public interface TokenProvider<T> {
	public T getToken(String raw);
}