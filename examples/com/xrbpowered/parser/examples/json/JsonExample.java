package com.xrbpowered.parser.examples.json;

import java.io.File;

public class JsonExample {

	public static final String EXAMPLE_PATH = "examples/com/xrbpowered/parser/examples/json/example.json";
	
	public static void main(String[] args) {
		Object obj = new JsonParser().parse(new File(EXAMPLE_PATH));
		if(obj!=null)
			new JsonPrinter().print(System.out, obj);
	}

}
