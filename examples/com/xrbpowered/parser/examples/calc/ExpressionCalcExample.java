package com.xrbpowered.parser.examples.calc;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.xrbpowered.parser.examples.calc.ast.Assignment;

public class ExpressionCalcExample {

	private static boolean printAST = true;

	public static void main(String[] args) {
		Map<String, Double> vars = new HashMap<>();
		ExpressionParser parser = new ExpressionParser(vars);

		try(Scanner scan = new Scanner(System.in)) {
			for(;;) {
				System.out.print("calc: ");
				String input = scan.nextLine();
				if(input.isBlank()) {
					System.out.print("done\n");
					return;
				}

				Assignment out = parser.parse(input);
				if(out != null) {
					if(printAST)
						out.printAST(System.out);
					out.exec(vars, System.out);
				}
			}
		}
	}

}
