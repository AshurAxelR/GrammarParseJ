package com.xrbpowered.parser.examples.json;

import java.util.regex.Pattern;

import com.xrbpowered.parser.err.TokenProviderException;
import com.xrbpowered.parser.err.TokeniserException;
import com.xrbpowered.parser.token.Tokeniser;
import com.xrbpowered.parser.token.TokeniserBuilder;

public class StringLiterals {

	private static Tokeniser<String> unescapeTokeniser = new TokeniserBuilder<String>()
			.rule("\\\\u([0-9a-fA-F]{4})", 1, (s) -> Character.toString((char) Integer.parseInt(s, 16)))
			.rule("\\\\.", (s) -> switch(s.charAt(1)) {
				case '\"' -> "\"";
				case '\\' -> "\\";
				case '/' -> "/";
				case 'b' -> "\b";
				case 'f' -> "\f";
				case 'n' -> "\n";
				case 'r' -> "\r";
				case 't' -> "\t";
				default -> throw new TokenProviderException("bad escape sequence \\"+s);
			})
			.rule(".", (s) -> s)
			.build();

	private static Tokeniser<String> escapeTokeniser = new TokeniserBuilder<String>()
			.rule(Pattern.compile(".", Pattern.MULTILINE+Pattern.DOTALL),
					(s) -> switch(s.charAt(0)) {
						case '\"' -> "\\\"";
						case '\\' -> "\\\\";
						case '\b' -> "\\b";
						case '\f' -> "\\f";
						case '\n' -> "\\n";
						case '\r' -> "\\r";
						case '\t' -> "\\t";
						default -> s;
					})
			.build();

	private StringLiterals() {}

	private static String process(String s, Tokeniser<String> t) {
		try {
			return Tokeniser.convertString(s, t);
		}
		catch (TokeniserException ex) {
			// can only happen for bad escape sequence
			throw (TokenProviderException) ex.getCause();
		}
	}
	
	public static String unescape(String s) {
		return process(s, unescapeTokeniser);
	}

	public static String escape(String s) {
		return process(s, escapeTokeniser);
	}

}
