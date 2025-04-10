package com.xrbpowered.parser.examples.calc;

import java.util.List;
import java.util.Map;

import com.xrbpowered.parser.err.OutputGeneratorException;
import com.xrbpowered.parser.err.ParserException;
import com.xrbpowered.parser.examples.calc.ast.Assignment;
import com.xrbpowered.parser.examples.calc.ast.BinaryOp;
import com.xrbpowered.parser.examples.calc.ast.ConstValue;
import com.xrbpowered.parser.examples.calc.ast.Expression;
import com.xrbpowered.parser.examples.calc.ast.Function;
import com.xrbpowered.parser.examples.calc.ast.UnaryOp;
import com.xrbpowered.parser.grammar.TokenisedGrammarParser;
import com.xrbpowered.parser.token.TokeniserBuilder;

public class ExpressionParser extends TokenisedGrammarParser<Object> {

	public ExpressionParser(Map<String, Double> vars) {
		super(new TokeniserBuilder<Object>()
				.rule("\\s+", null)
				.rule("[0-9]+(\\.[0-9]+)?", (s) -> Double.parseDouble(s))
				.rule("[A-Za-z][A-Za-z_0-9]*", (s) -> s)
				.rule("[()+\\-*/=,]", (s) -> s.charAt(0))
				.build());
		
		rule("top_expr", Assignment.class)
			.sel(q(String.class, '=', r("expr")), (vs) -> new Assignment((String) vs[0], (Expression) vs[2]))
			.sel(q(r("expr")), (vs) -> new Assignment(null, (Expression) vs[0]));
		
		rule("expr", Expression.class)
			.sel(q(r("expr_prod"), '+', r("expr")), (vs) -> BinaryOp.add((Expression) vs[0], (Expression) vs[2]))
			.sel(q(r("expr_prod"), '-', r("expr")), (vs) -> BinaryOp.sub((Expression) vs[0], (Expression) vs[2]))
			.sel(q(r("expr_prod")), (vs) -> (Expression) vs[0]);
		
		rule("expr_prod", Expression.class)
			.sel(q(r("expr_sign"), '*', r("expr_prod")), (vs) -> BinaryOp.mul((Expression) vs[0], (Expression) vs[2]))
			.sel(q(r("expr_sign"), '/', r("expr_prod")), (vs) -> BinaryOp.div((Expression) vs[0], (Expression) vs[2]))
			.sel(q(r("expr_sign")), (vs) -> (Expression) vs[0]);
		
		rule("expr_sign", Expression.class)
			.sel(q('-', r("expr_lit")), (vs) -> UnaryOp.neg((Expression) vs[1]))
			.sel(q('+', r("expr_lit")), (vs) -> (Expression) vs[1]) // unary plus does nothing
			.sel(q(r("expr_lit")), (vs) -> (Expression) vs[0]);

		rule("expr_lit", Expression.class)
			.sel(q('(', r("expr"), ')'), (vs) -> (Expression) vs[1])
			.sel(q(String.class, '(', r("opt_args"), ')'), (vs) -> {
				@SuppressWarnings("unchecked")
				List<Expression> args = (List<Expression>) vs[2];
				return Function.create((String) vs[0], args);
			})
			.sel(q(String.class), (vs) -> {
				String name = (String) vs[0];
				Double val = vars.get(name);
				if(val==null)
					throw new OutputGeneratorException("unknown variable " + name);
				return new ConstValue(val); // reading variable value as constant
			})
			.sel(q(Double.class), (vs) -> new ConstValue((Double) vs[0]));

		optListRule("opt_args", r("expr"), ',', Expression.class);

		linkRules("top_expr");
	}
	
	@Override
	protected boolean lookingAt(Object o) {
		if(o instanceof Class<?> cls)
			return cls.isInstance(token);
		else if(o instanceof Character ch)
			return (token instanceof Character tch) && ch.charValue()==tch.charValue();
		else
			return false;
	}
	
	@Override
	public Object tokenValue() {
		return token;
	}
	
	public Assignment parse(String input) {
		try {
			tokeniser.start(input);
			return (Assignment) parseInput();
		}
		catch(ParserException ex) {
			System.err.printf("(%d) %s\n", ex.pos, ex.getMessage());
			return null;
		}
	}

}
