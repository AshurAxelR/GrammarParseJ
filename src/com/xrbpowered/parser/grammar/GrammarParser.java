package com.xrbpowered.parser.grammar;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.RuleMatchingException;
import com.xrbpowered.parser.err.UnexpectedTokenException;

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
	
	public static class OptionalPattern {
		public Object[] p;
		public OptionalPattern(Object[] p) {
			this.p = p;
		}
		@Override
		public String toString() {
			return String.format("opt(%s)", Arrays.stream(p)
					.map(Object::toString).collect(Collectors.joining(",")));
		}
	}
	
	protected Map<String, GrammarRule<?>> rules = new LinkedHashMap<>();
	
	private GrammarRule<?> topRule = null;

	public abstract int getPos();
	public abstract boolean isEnd();
	
	protected abstract void next() throws ParserException;
	protected abstract void restorePos(int index);
	
	protected abstract boolean lookingAt(Object o);
	public abstract Object tokenValue();
	
	protected ParserException lastError = null;
	
	private Object matchOptional(OptionalPattern opt) throws ParserException {
		int pos = getPos();
		try {
			Object[] vs = new Object[opt.p.length];
			for(int i=0; i<opt.p.length; i++)
				vs[i] = match(opt.p[i]);
			return vs;
		}
		catch(RuleMatchingException ex) {
			restorePos(pos);
			return null;
		}
	}
	
	protected Object match(Object p) throws ParserException {
		if(isEnd())
			throw new RuleMatchingException(this, "unexpected end of file");
		else if(p instanceof GrammarRule<?> rule)
			return rule.lookingAt(this);
		else if(p instanceof OptionalPattern opt)
			return matchOptional(opt);
		else if(lookingAt(p)) {
			Object v = tokenValue();
			next();
			return v;
		}
		else
			throw new UnexpectedTokenException(this);
	}
	
	private void addRule(GrammarRule<?> r) {
		if(rules.containsKey(r.name))
			throw new InvalidParameterException(String.format("rule %s already exists", r.name));
		rules.put(r.name, r);
	}
	
	protected <R> GrammarRule<R> rule(String name, Class<R> output) {
		GrammarRule<R> r = new GrammarRule<>(name, output);
		addRule(r);
		return r;
	}
	
	@SuppressWarnings("unchecked")
	protected <R> void listRule(String name, Object item, Object sep, Class<R> itemClass) {
		GrammarRule<List<R>> r = new GrammarRule<>(name, itemClass);
		addRule(r);
		
		r.sel(q(item), (vs) -> {
			LinkedList<R> list = new LinkedList<>();
			R v = (R) vs[0];
			list.addFirst(v);
			return list;
		});
		
		if(sep==null) {
			r.sel(q(item, r(name)), (vs) -> {
				LinkedList<R> list = (LinkedList<R>) vs[1];
				R v = (R) vs[0];
				list.addFirst(v);
				return list;
			});
		}
		else {
			r.sel(q(item, sep, r(name)), (vs) -> {
				LinkedList<R> list = (LinkedList<R>) vs[2];
				R v = (R) vs[0];
				list.addFirst(v);
				return list;
			});
		}
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
	
	protected static Object linkPatternRule(GrammarParser parser, Object p) {
		if(p==null)
			return null;
		else if(p instanceof RuleRef ref) {
			GrammarRule<?> rule = parser.rules.get(ref.name);
			if(rule==null)
				throw new InvalidParameterException("no parser rule "+ref.name);
			return rule;
		}
		else if(p instanceof OptionalPattern opt) {
			for(int i=0; i<opt.p.length; i++)
				opt.p[i] = linkPatternRule(parser, opt.p[i]);
			return p;
		}
		else
			return p;
	}

	public static Object optValue(Object optv, int index, Object def) {
		if(optv==null)
			return def;
		else
			return ((Object[]) optv)[index];
	}
	
	public static RuleRef r(String name) {
		return new RuleRef(name);
	}
	
	public static Object[] q(Object... objects) {
		return objects;
	}
	
	public static OptionalPattern opt(Object... p) {
		if(p.length==0)
			throw new InvalidParameterException("optional pattern must have at leasst one element");
		return new OptionalPattern(p);
	}

}
