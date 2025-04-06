package com.xrbpowered.parser.grammar;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.xrbpowered.parser.ParserException;

public abstract class GrammarParser {

	public static class RuleRef {
		public final String name;
		public RuleRef(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
		@Override
		public boolean equals(Object obj) {
			if(this==obj)
				return true;
			if(!(obj instanceof RuleRef))
				return false;
			RuleRef other = (RuleRef) obj;
			return Objects.equals(name, other.name);
		}
	}
	
	protected Map<String, GrammarRule<?>> rules = new LinkedHashMap<>();
	
	private GrammarRule<?> topRule = null;

	protected abstract int getPos();
	public abstract int lineIndex();
	public abstract boolean isEnd();
	
	protected abstract void next() throws ParserException;
	protected abstract void restorePos(int index);
	
	protected abstract boolean lookingAt(Object o);
	protected abstract Object tokenValue();
	
	public <R> GrammarRule<R> rule(String name, Class<R> output) {
		if(rules.containsKey(name))
			throw new InvalidParameterException(String.format("rule %s already exists", name));
		GrammarRule<R> r = new GrammarRule<R>(name, output);
		rules.put(name, r);
		return r;
	}
	
	public GrammarRule<?> getTopRule() {
		return topRule;
	}
	
	public void linkRules(String topRule) {
		this.topRule = rules.get(topRule);
		if(this.topRule==null)
			throw new InvalidParameterException("no parser rule "+topRule);
		for(GrammarRule<?> rule : rules.values())
			rule.linkRules(this);
	}
	
	public void printPatterns(PrintStream out) {
		for(Entry<String, GrammarRule<?>> e : rules.entrySet()) {
			out.printf("%s :=", e.getKey());
			e.getValue().printPattern(out);
		}
	}
	
	public static RuleRef r(String name) {
		return new RuleRef(name);
	}
	
	public static Object[] q(Object... objects) {
		return objects;
	}

}
