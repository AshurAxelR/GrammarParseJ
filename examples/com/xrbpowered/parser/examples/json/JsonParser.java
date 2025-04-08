package com.xrbpowered.parser.examples.json;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.grammar.TokenisedGrammarParser;
import com.xrbpowered.parser.token.TokeniserBuilder;

public class JsonParser extends TokenisedGrammarParser<JsonParser.Token> {

	private enum Type {
		SYMBOL, STRING, LITERAL
	}
	
	static class Token {
		public final Type type;
		public final Object value;
		
		public Token(Type type, Object value) {
			this.type = type;
			this.value = value;
		}
		
		public static Token fromKeyword(String s) {
			return new Token(Type.LITERAL, switch(s) {
				case "true" -> true;
				case "false" -> false;
				case "null" -> null;
				default -> throw new RuntimeException("unknown keyword");
			});
		}
	}
	
	private static class KeyValue {
		public final String key;
		public final Object value;
		
		public KeyValue(String key, Object value) {
			this.key = key;
			this.value = value;
		}
	}
	
	public JsonParser() {
		super(new TokeniserBuilder<JsonParser.Token>()
				.rule("[\\s\\n]+", Pattern.MULTILINE, null)
				.rule("-?[0-9]+\\.[0-9]+", (s) -> new Token(Type.LITERAL, Double.parseDouble(s)))
				.rule("-?[1-9][0-9]*", (s) -> new Token(Type.LITERAL, Integer.parseInt(s)))
				.rule("\"[^\"]*\"", (s) -> new Token(Type.STRING, s.substring(1, s.length()-1)))
				.rule("[A-Za-z][A-Za-z_0-9]*", (s) -> Token.fromKeyword(s))
				.rule("[\\[\\]{}:,]", (s) -> new Token(Type.SYMBOL, s.charAt(0)))
				.build());
		
		rule("value", Object.class)
			.sel(q('{', opt(r("key_values")), '}'), (vs) -> {
				Map<String, Object> map = new LinkedHashMap<>();
				@SuppressWarnings("unchecked")
				List<KeyValue> kvs = (List<KeyValue>) optValue(vs[1], 0, List.of());
				for(KeyValue kv : kvs)
					map.put(kv.key, kv.value);
				return map;
			})
			.sel(q('[', opt(r("array_items")), ']'), (vs) -> optValue(vs[1], 0, List.of()))
			.sel(q(Type.STRING), (vs) -> vs[0])
			.sel(q(Type.LITERAL), (vs) -> vs[0]);
		
		listRule("array_items", r("value"), ',', Object.class);
		listRule("key_values", r("key_value"), ',', KeyValue.class);
		
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
	public Object tokenValue() {
		return token.value;
	}
	
	public Object parse(File file) {
		try {
			tokeniser.start(file);
			next();
			return getTopRule().lookingAt(this);
		}
		catch (ParserException|IOException ex) {
			// TODO print line:col
			System.err.println(ex.getMessage());
			return null;
		}
	}
	
	public void printTokens(File file) {
		try {
			tokeniser.start(file);
			next();
			while(!isEnd()) {
				System.out.printf("%s:%s\n", token.type.name(), token.value);
				next();
			}
		}
		catch (ParserException|IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

}
