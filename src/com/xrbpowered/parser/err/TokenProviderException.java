package com.xrbpowered.parser.err;

public class TokenProviderException extends RuntimeException {

	public TokenProviderException() {
	}

	public TokenProviderException(String msg) {
		super(msg);
	}

	public TokenProviderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
