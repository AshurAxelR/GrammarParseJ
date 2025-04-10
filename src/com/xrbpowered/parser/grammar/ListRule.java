package com.xrbpowered.parser.grammar;

import java.util.ArrayList;
import java.util.List;

import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.err.RuleMatchingException;
import com.xrbpowered.parser.err.UnexpectedTokenException;

public class ListRule<V> extends ParserRule {

	private Object item;
	private Object sep;
	private boolean opt;
	
	public ListRule(String name, boolean opt, Object item, Object sep) {
		super(name);
		this.item = item;
		this.sep = sep;
		this.opt = opt;
	}

	@Override
	public void linkRules(GrammarParser parser) {
		item = parser.linkPatternRule(item);
		sep = parser.linkPatternRule(sep);
	}

	@Override
	protected List<V> lookingAt(boolean top, GrammarParser parser) throws ParserException {
		List<V> list = new ArrayList<>();
		int pos = parser.getPos();
		
		boolean next = true;
		while(next) {
			try {
				@SuppressWarnings("unchecked")
				V v = (V) parser.match(item);
				list.add(v);
				pos = parser.getPos();
				parser.match(sep);
				next = true;
			}
			catch (RuleMatchingException ex) {
				// didn't match, roll back
				if(parser.lastError==null || ex.pos>parser.lastError.pos)
					parser.lastError = ex;
				parser.restorePos(pos);
				next = false;
			}
		}
		
		if(list.isEmpty() && !opt)
			throw new UnexpectedTokenException(parser); 
		else
			return list;
	}

}
