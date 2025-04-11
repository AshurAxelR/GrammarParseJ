package com.xrbpowered.parser.examples.json;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.TokenProviderException;
import com.xrbpowered.parser.grammar.TokenisedGrammarParser;
import com.xrbpowered.parser.token.LineColProvider.LineColPos;
import com.xrbpowered.parser.token.LineColProvider;
import com.xrbpowered.parser.token.TokeniserBuilder;

public class JsonParser extends TokenisedGrammarParser<JsonParser.Token> {

	public static int tabSize = 4;
	
	private enum Type {
		SYMBOL, STRING, LITERAL
	}
	
	protected static record Token(Type type, Object value) {}

	public static Token fromKeyword(String s) {
		return new Token(Type.LITERAL, switch(s) {
			case "true" -> true;
			case "false" -> false;
			case "null" -> null;
			default -> throw new TokenProviderException("unknown keyword "+s);
		});
	}

	private static record KeyValue(String key, Object value) {}
	
	public JsonParser() {
		super(new TokeniserBuilder<JsonParser.Token>()
				.rule(Pattern.compile("[\\s\\n]+", Pattern.MULTILINE), null)
				.ruleEx("-?(?:0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+\\-]?[0-9]+)?", (m) -> {
					if(m.group(1)==null && m.group(2)==null) {
						// if no fractional or exponent parts, parse into Long
						return new Token(Type.LITERAL, Long.parseLong(m.group()));
					}
					else
						return new Token(Type.LITERAL, Double.parseDouble(m.group()));
				})
				.rule("\"([^\"]*)\"", 1, (s) -> new Token(Type.STRING, StringLiterals.unescape(s)))
				.rule("[A-Za-z][A-Za-z_0-9]*", (s) -> fromKeyword(s))
				.rule("[\\[\\]{}:,]", (s) -> new Token(Type.SYMBOL, s.charAt(0)))
				.build());
		
		rule("value", Object.class)
			.sel(q('{', r("opt_key_values"), '}'), (vs) -> {
				Map<String, Object> map = new LinkedHashMap<>();
				@SuppressWarnings("unchecked")
				List<KeyValue> kvs = (List<KeyValue>) vs[1];
				for(KeyValue kv : kvs)
					map.put(kv.key, kv.value);
				return map;
			})
			.sel(q('[', r("opt_array_items"), ']'), (vs) -> vs[1])
			.sel(q(Type.STRING), (vs) -> vs[0])
			.sel(q(Type.LITERAL), (vs) -> vs[0]);
		
		optListRule("opt_array_items", r("value"), ',', Object.class);
		optListRule("opt_key_values", r("key_value"), ',', KeyValue.class);
		
		rule("key_value", KeyValue.class)
			.sel(q(Type.STRING, ':', r("value")), (vs) -> new KeyValue((String) vs[0], vs[2]));

		linkRules("value");
	}
	
	@Override
	protected boolean lookingAt(Object o) {
		if(o instanceof Type type)
			return token.type==type;
		else if(o instanceof Character)
			return token.type==Type.SYMBOL && o.equals(token.value);
		else
			return false;
	}

	@Override
	public Object tokenValue(Token token) {
		return token.value;
	}
	
	@Override
	public String tokenName(Token token) {
		return switch(token.type) {
			case SYMBOL -> String.format("symbol '%s'", token.value);
			case STRING ->  "string";
			case LITERAL -> String.format("literal: %s", token.value);
		};
	}
	
	protected Object parseInput() {
		try {
			return super.parseInput();
		}
		catch (ParserException ex) {
			LineColPos lc = new LineColProvider(tokeniser.getSource()).find(ex.pos);
			System.err.printf("(%d:%d) %s\n", lc.line()+1, lc.col()+1, ex.getMessage());
			return null;
		}
	}

	public Object parse(File file) {
		try {
			tokeniser.start(file);
			return parseInput();
		}
		catch(IOException ex) {
			System.err.println(ex.getMessage());
			return null;
		}
	}

	public Object parse(String s) {
		tokeniser.start(s);
		return parseInput();
	}

}
