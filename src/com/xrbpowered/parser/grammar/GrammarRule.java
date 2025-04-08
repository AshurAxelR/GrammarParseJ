package com.xrbpowered.parser.grammar;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.xrbpowered.parser.err.OutputGeneratorException;
import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.RuleMatchingException;

public class GrammarRule<R> {

	private class Node {
		private final int d;
		private Object p;
		private Map<Object, Node> next = new LinkedHashMap<>();
		private OutputGenerator<?> gen = null;
		
		public Node(int d, Object p) {
			if(d>0 && p==null)
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
			if(n==null) {
				n = new Node(d+1, p);
				this.next.put(p, n);
			}
			n.add(pattern, gen);
		}
		
		public void linkRules(GrammarParser parser) {
			p = GrammarParser.linkPatternRule(parser, p);
			for(Node n : next.values())
				n.linkRules(parser);
		}

		public Object lookingAt(GrammarParser parser, int ruleStartPos, Deque<Object> vs) throws ParserException {
			if(d>0) {
				// test pattern and append token value
				vs.add(parser.match(p));
			}

			RuleMatchingException lastErr = null;
			if(next.isEmpty()) {
				if(top && !parser.isEnd()) {
					if(parser.lastError!=null)
						throw parser.lastError;
					else
						throw new RuleMatchingException(parser, "expected end of file");
				}
			}
			else {
				// continue pattern
				int vsLen = vs.size();
				int pos = parser.getPos();
				for(Node n : next.values()) {
					try {
						return n.lookingAt(parser, ruleStartPos, vs);
					}
					catch(RuleMatchingException ex) {
						// didn't match, roll back
						if(lastErr==null || ex.pos>lastErr.pos)
							lastErr = ex;
						if(parser.lastError==null || ex.pos>parser.lastError.pos)
							parser.lastError = ex;
						parser.restorePos(pos);
						while(vs.size()>vsLen)
							vs.removeLast();
					}
				}
			}
			
			// reached end of pattern, must generate output
			if(gen!=null) {
				try {
					return gen.gen(vs.toArray(n -> new Object[n]));
				}
				catch (OutputGeneratorException ex) {
					// e.printStackTrace();
					throw new ParserException(ruleStartPos, ex);
				}
			}
			else if(lastErr!=null)
				throw lastErr;
			else
				throw new InvalidParameterException("no output generator");
		}
		
		public void printPattern(PrintStream out) {
			for(int i=0; i<d; i++)
				out.print("-");
			if(p != null)
				out.printf("%s: %s", p.getClass().getSimpleName(), p.toString());
			if(gen != null)
				out.print(" > out");
			out.print("\n");
			for(Node n : next.values())
				n.printPattern(out);
		}
	}
	
	public final String name;
	public final Class<?> output;
	
	private Node root = new Node(0, null);
	private boolean top = false;
	
	public GrammarRule(String name, Class<?> output) {
		this.name = name;
		this.output = output;
	}
	
	public GrammarRule<R> sel(Object[] pattern, OutputGenerator<R> gen) {
		root.add(pattern, gen);
		return this;
	}
	
	public void linkRules(GrammarParser parser) {
		this.top = (this==parser.getTopRule());
		root.linkRules(parser);
	}
	
	public Object lookingAt(GrammarParser parser) throws ParserException {
		if(top)
			parser.lastError = null;
		Deque<Object> vs = new LinkedList<>();
		return root.lookingAt(parser, parser.getPos(), vs);
	}
	
	public void printPattern(PrintStream out) {
		root.printPattern(out);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
