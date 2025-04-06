package com.xrbpowered.parser.grammar;

import java.io.PrintStream;
import java.security.InvalidParameterException;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.xrbpowered.parser.ParserException;
import com.xrbpowered.parser.grammar.GrammarParser.RuleRef;

public class GrammarRule<R> {

	public static class UnexpectedTokenException extends ParserException {

		public UnexpectedTokenException(GrammarParser parser, String fmt) {
			super(parser.getPos(), parser.lineIndex(), String.format(fmt, parser.tokenValue()));
		}

		public UnexpectedTokenException(GrammarParser parser) {
			this(parser, "unexpected token %s");
		}

	}
	
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
			if(p!=null && p instanceof RuleRef ref) {
				GrammarRule<?> rule = parser.rules.get(ref.name);
				if(rule==null)
					throw new InvalidParameterException("no parser rule "+ref.name);
				this.p = rule;
			}
			for(Node n : next.values())
				n.linkRules(parser);
		}

		public Object lookingAt(GrammarParser parser, Deque<Object> vs) throws ParserException {
			if(d>0) {
				if(parser.isEnd())
					throw new ParserException(parser.getPos(), parser.lineIndex(), "unexpected end of file");
				else if(p instanceof GrammarRule<?> rule) {
					Object r = rule.lookingAt(parser);
					vs.add(r);
				}
				else if(parser.lookingAt(p)) {
					vs.add(parser.tokenValue());
					parser.next();
				}
				else
					throw new UnexpectedTokenException(parser);
			}

			ParserException lastEx = null;
			if(next.isEmpty()) {
				if(top && !parser.isEnd())
					throw new UnexpectedTokenException(parser, "expected end of file, got %s");
			}
			else {
				int vsLen = vs.size();
				int pos = parser.getPos();
				for(Node n : next.values()) {
					try {
						return n.lookingAt(parser, vs);
					}
					catch(ParserException ex) {
						if(ex.getCause()!=null)
							throw ex;
						if(lastEx==null || ex.pos>lastEx.pos)
							lastEx = ex;
						parser.restorePos(pos);
						while(vs.size()>vsLen)
							vs.removeLast();
					}
				}
			}
			
			if(gen!=null) {
				try {
					return gen.gen(vs.toArray(n -> new Object[n]));
				}
				catch (RuntimeException e) {
					// e.printStackTrace();
					throw new ParserException(parser.getPos(), parser.lineIndex(), e);
				}
			}
			else if(lastEx!=null)
				throw lastEx;
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
		Deque<Object> vs = new LinkedList<>();
		return root.lookingAt(parser, vs);
	}
	
	public void printPattern(PrintStream out) {
		root.printPattern(out);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
