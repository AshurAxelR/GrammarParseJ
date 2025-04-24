package com.xrbpowered.parser.grammar;

import java.util.Deque;
import java.util.LinkedList;

import com.xrbpowered.parser.err.OutputGeneratorException;
import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.RuleMatchingException;

public class BinaryOpRule<V> extends ParserRule {

	private class Tail {
		public Object op;
		public V arg;
		public int pos;

		public Tail(Object op, V arg, int pos) {
			this.op = op;
			this.arg = arg;
			this.pos = pos;
		}
	}

	private Object arg;
	private Object[] ops;
	private boolean right;
	private OutputGenerator<V> gen;

	public BinaryOpRule(String name, boolean right, Object arg, Object[] ops, OutputGenerator<V> gen) {
		super(name);
		this.arg = arg;
		this.ops = ops.clone();
		this.right = right;
		this.gen = gen;
	}

	@Override
	public void linkRules(GrammarParser parser) {
		arg = parser.linkPatternRule(arg);
		for(int i = 0; i < ops.length; i++)
			ops[i] = parser.linkPatternRule(ops[i]);
	}

	private Object matchOp(GrammarParser parser, int pos) throws ParserException {
		RuleMatchingException lastErr = null;
		for(Object op : ops) {
			try {
				return parser.match(op);
			}
			catch(RuleMatchingException ex) {
				// didn't match, roll back
				if(lastErr == null || ex.pos > lastErr.pos)
					lastErr = ex;
				parser.rollBack(pos, ex);
			}
		}
		throw lastErr;
	}

	private V fold(V head, Deque<Tail> tail) throws ParserException {
		if(tail == null)
			return head;

		V node = head;
		while(!tail.isEmpty()) {
			Tail next = tail.removeFirst();
			try {
				Object[] vs;
				if(right)
					vs = new Object[] {next.arg, next.op, node};
				else
					vs = new Object[] {node, next.op, next.arg};
				node = gen.gen(vs);
			}
			catch(OutputGeneratorException ex) {
				throw new ParserException(next.pos, ex.getMessage(), ex);
			}
		}
		return node;
	}

	@Override
	protected V lookingAt(boolean top, GrammarParser parser) throws ParserException {
		@SuppressWarnings("unchecked")
		V head = (V) parser.match(arg);
		Deque<Tail> tail = null;
		int pos = parser.getPos();

		boolean next = true;
		while(next) {
			try {
				Object op = matchOp(parser, pos);
				@SuppressWarnings("unchecked")
				V v = (V) parser.match(arg);

				if(tail == null)
					tail = new LinkedList<>();
				if(right) {
					tail.addFirst(new Tail(op, head, pos));
					head = v;
				}
				else {
					tail.addLast(new Tail(op, v, pos));
				}

				pos = parser.getPos();
				next = true;
			}
			catch(RuleMatchingException ex) {
				// didn't match, roll back
				parser.rollBack(pos, ex);
				next = false;
			}
		}
		return fold(head, tail);
	}

	public static <V> ParserRule createPrecRules(String name, boolean right, Class<V> nodeClass,
			Object arg, Object[][] ops, OutputGenerator<V> gen) {
		ParserRule rule = null;
		Object a = arg;
		for(int i = ops.length - 1; i >= 0; i--) {
			String n = (i == 0) ? name : String.format("%s:%d", name, i);
			rule = new BinaryOpRule<V>(n, right, a, ops[i], gen);
			a = rule;
		}
		return rule;
	}

}
