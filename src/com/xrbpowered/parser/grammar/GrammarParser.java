package com.xrbpowered.parser.grammar;

import java.security.InvalidParameterException;
import java.util.Arrays;
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
			if(this == obj)
				return true;
			if(getClass() != obj.getClass())
				return false;
			RuleRef other = (RuleRef) obj;
			return Objects.equals(name, other.name);
		}
	}

	public abstract static class MultiPattern {
		public Object[] p;

		public MultiPattern(Object[] p) {
			if(p.length < 1)
				throw new IllegalArgumentException("pattern must have at least one element");
			this.p = p;
		}

		public abstract Object match(GrammarParser parser) throws ParserException;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.deepHashCode(p);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(getClass() != obj.getClass())
				return false;
			MultiPattern other = (MultiPattern) obj;
			return Arrays.deepEquals(p, other.p);
		}
	}

	public static class OptionalPattern extends MultiPattern {
		public OptionalPattern(Object[] p) {
			super(p);
		}

		@Override
		public Object match(GrammarParser parser) throws ParserException {
			int pos = parser.getPos();
			try {
				Object[] vs = new Object[p.length];
				for(int i = 0; i < p.length; i++)
					vs[i] = parser.match(p[i]);
				return vs;
			}
			catch(RuleMatchingException ex) {
				parser.restorePos(pos);
				return null;
			}
		}
	}

	public static class AnyPattern extends MultiPattern {
		public AnyPattern(Object[] p) {
			super(p);
		}

		@Override
		public Object match(GrammarParser parser) throws ParserException {
			int pos = parser.getPos();
			for(Object p : this.p) {
				try {
					return parser.match(p);
				}
				catch(RuleMatchingException ex) {
					parser.restorePos(pos);
				}
			}
			throw new UnexpectedTokenException(parser);
		}
	}

	protected Map<String, ParserRule> rules = new LinkedHashMap<>();

	protected ParserRule topRule = null;
	protected ParserException lastError = null;

	public abstract int getPos();
	public abstract boolean isEnd();

	protected abstract void next() throws ParserException;
	protected abstract void restorePos(int pos) throws ParserException;

	protected abstract boolean lookingAt(Object o);
	public abstract Object tokenValue();

	public String tokenName() {
		return String.format("token: %s", tokenValue());
	}

	protected void rollBack(int pos, RuleMatchingException ex) throws ParserException {
		if(lastError == null || ex.pos > lastError.pos)
			lastError = ex;
		restorePos(pos);
	}

	protected Object match(Object p) throws ParserException {
		if(isEnd())
			throw new RuleMatchingException(this, "unexpected end of file");
		else if(p instanceof ParserRule rule)
			return rule.lookingAt(false, this);
		else if(p instanceof OptionalPattern opt)
			return opt.match(this);
		else if(p instanceof AnyPattern any)
			return any.match(this);
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
			throw new InvalidParameterException(String.format("rule <%s> already exists", r.name));
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

	protected <V> void binaryOpRuleL(String name, Class<V> nodeClass,
			Object arg, Object[] ops, OutputGenerator<V> gen) {
		addRule(new BinaryOpRule<V>(name, false, arg, ops, gen));
	}

	protected <V> void binaryOpRuleR(String name, Class<V> nodeClass,
			Object arg, Object[] ops, OutputGenerator<V> gen) {
		addRule(new BinaryOpRule<V>(name, true, arg, ops, gen));
	}

	protected <V> void binaryOpPrecRulesL(String name, Class<V> nodeClass,
			Object arg, Object[][] ops, OutputGenerator<V> gen) {
		addRule(BinaryOpRule.createPrecRules(name, false, nodeClass, arg, ops, gen));
	}

	protected <V> void binaryOpPrecRulesR(String name, Class<V> nodeClass,
			Object arg, Object[][] ops, OutputGenerator<V> gen) {
		addRule(BinaryOpRule.createPrecRules(name, true, nodeClass, arg, ops, gen));
	}

	public ParserRule getRule(String name) {
		ParserRule rule = rules.get(name);
		if(rule == null)
			throw new InvalidParameterException(String.format("no parser rule <%s>", name));
		return rule;
	}

	public void linkRules(String topRule) {
		this.topRule = getRule(topRule);
		for(ParserRule rule : rules.values())
			rule.linkRules(this);
	}

	public Object linkPatternRule(Object p) {
		if(p == null)
			return null;
		else if(p instanceof ParserRule rule) {
			rule.linkRules(this);
			return p;
		}
		else if(p instanceof RuleRef ref)
			return getRule(ref.name);
		else if(p instanceof MultiPattern mp) {
			for(int i = 0; i < mp.p.length; i++)
				mp.p[i] = linkPatternRule(mp.p[i]);
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
		catch(ParserException ex) {
			if(lastError != null && lastError.pos > ex.pos)
				throw lastError;
			else
				throw ex;
		}
	}

	public static Object optValue(Object optv, int index, Object def) {
		if(optv == null)
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
		return new OptionalPattern(p);
	}

	public static AnyPattern any(Object... p) {
		return new AnyPattern(p);
	}

}
