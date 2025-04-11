package com.xrbpowered.parser.grammar;

import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
	}
	
	protected Map<String, ParserRule> rules = new LinkedHashMap<>();
	
	protected ParserRule topRule = null;

	public abstract int getPos();
	public abstract boolean isEnd();
	
	protected abstract void next() throws ParserException;
	protected abstract void restorePos(int index) throws ParserException;
	
	protected abstract boolean lookingAt(Object o);
	public abstract Object tokenValue();

	public String tokenName() {
		return String.format("token: %s", tokenValue());
	}

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
		else if(p instanceof ParserRule rule)
			return rule.lookingAt(false, this);
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
	
	protected void addRule(ParserRule r) {
		if(rules.containsKey(r.name))
			throw new InvalidParameterException(String.format("rule %s already exists", r.name));
		rules.put(r.name, r);
	}
	
	protected <V> GrammarRule<V> rule(String name, Class<V> output) {
		GrammarRule<V> r = new GrammarRule<>(name);
		addRule(r);
		return r;
	}
	
	protected <V> void listRule(String name, Object item, Object sep, Class<V> itemClass) {
		addRule(new ListRule<V>(name, false, item, sep));
	}

	protected <V> void optListRule(String name, Object item, Object sep, Class<V> itemClass) {
		addRule(new ListRule<V>(name, true, item, sep));
	}

	public ParserRule getRule(String name) {
		ParserRule rule = rules.get(name);
		if(rule==null)
			throw new InvalidParameterException("no parser rule "+name);
		return rule;
	}
	
	public void linkRules(String topRule) {
		this.topRule = getRule(topRule);
		for(ParserRule rule : rules.values())
			rule.linkRules(this);
	}
	
	public Object linkPatternRule(Object p) {
		if(p==null)
			return null;
		else if(p instanceof RuleRef ref)
			return getRule(ref.name);
		else if(p instanceof OptionalPattern opt) {
			for(int i=0; i<opt.p.length; i++)
				opt.p[i] = linkPatternRule(opt.p[i]);
			return p;
		}
		else
			return p;
	}
	
	protected Object parseInput() throws ParserException {
		next();
		lastError = null;
		try {
			return topRule.lookingAt(true, this);
		}
		catch (ParserException ex) {
			if(lastError!=null && lastError.pos>ex.pos)
				throw lastError;
			else
				throw ex;
		}
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
