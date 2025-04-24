package com.xrbpowered.parser.grammar;

import java.security.InvalidParameterException;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.xrbpowered.parser.err.OutputGeneratorException;
import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.RuleMatchingException;

public class GrammarRule<V> extends ParserRule {

	private class Node {
		private final int d;
		private Object p;
		private Map<Object, Node> next = new LinkedHashMap<>();
		private OutputGenerator<?> gen = null;

		public Node(int d, Object p) {
			if(d > 0 && p == null)
				throw new InvalidParameterException("null pattern");
			this.d = d;
			this.p = p;
		}

		public void add(Object[] pattern, OutputGenerator<?> gen) {
			if(pattern.length <= this.d) {
				if(this.gen != null)
					throw new InvalidParameterException("rule pattern collision");
				this.gen = gen;
				return;
			}
			Object p = pattern[d];
			Node n = this.next.get(p);
			if(n == null) {
				n = new Node(d + 1, p);
				this.next.put(p, n);
			}
			n.add(pattern, gen);
		}

		public void linkRules(GrammarParser parser) {
			p = parser.linkPatternRule(p);
			for(Node n : next.values())
				n.linkRules(parser);
		}

		public Object lookingAt(GrammarParser parser, boolean top, int ruleStartPos,
				Deque<Object> vs) throws ParserException {

			if(d > 0) {
				// test pattern and append token value
				vs.add(parser.match(p));
			}

			RuleMatchingException lastErr = null;
			if(next.isEmpty()) {
				if(top && !parser.isEnd()) {
					if(parser.lastError != null && parser.lastError.pos >= parser.getPos())
						throw parser.lastError;
					else
						throw new ParserException(parser.getPos(), "expected end of file");
				}
			}
			else {
				// continue pattern
				int vsLen = vs.size();
				int pos = parser.getPos();
				for(Node n : next.values()) {
					try {
						return n.lookingAt(parser, top, ruleStartPos, vs);
					}
					catch(RuleMatchingException ex) {
						// didn't match, roll back
						if(lastErr == null || ex.pos > lastErr.pos)
							lastErr = ex;
						parser.rollBack(pos, ex);
						while(vs.size() > vsLen)
							vs.removeLast();
					}
				}
			}

			// reached end of pattern, must generate output
			if(gen != null) {
				try {
					return gen.gen(vs.toArray(n -> new Object[n]));
				}
				catch(OutputGeneratorException ex) {
					throw new ParserException(ruleStartPos, ex.getMessage(), ex);
				}
			}
			else if(lastErr != null)
				throw lastErr;
			else
				throw new InvalidParameterException("no output generator");
		}
	}

	protected Node root = new Node(0, null);

	public GrammarRule(String name) {
		super(name);
	}

	public GrammarRule<V> sel(Object[] pattern, OutputGenerator<V> gen) {
		root.add(pattern, gen);
		return this;
	}

	@Override
	public void linkRules(GrammarParser parser) {
		root.linkRules(parser);
	}

	@Override
	protected Object lookingAt(boolean top, GrammarParser parser) throws ParserException {
		return root.lookingAt(parser, top, parser.getPos(), new LinkedList<>());
	}

}
