package com.xrbpowered.parser.examples.json;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class JsonPrinter {

	private boolean pretty;
	private String indent;
	
	public JsonPrinter(boolean pretty, String indent) {
		this.pretty = pretty;
		this.indent = indent;
	}

	public JsonPrinter() {
		this(false, "");
	}

	private void newline(PrintStream out, String indent) {
		if(pretty) {
			out.print("\n");
			out.print(indent);
		}
	}
	
	private void print(PrintStream out, String indent, Object o) {
		if(o==null)
			out.print("null");
		else if(o instanceof Map<?, ?> map) {
			out.print("{");
			if(!map.isEmpty()) {
				String nextIndent = indent + this.indent;
				newline(out, nextIndent);
				boolean first = true;
				for(Map.Entry<?, ?> e : map.entrySet()) {
					Object key = e.getKey();
					if(key instanceof String s) {
						if(!first) {
							out.print(",");
							newline(out, nextIndent);
						}
						first = false;
						out.printf("\"%s\":", s);
						if(pretty)
							out.print(" ");
						print(out, nextIndent, e.getValue());
					}
					else {
						// TODO throw exception
					}
				}
				newline(out, indent);
			}
			out.print("}");
		}
		else if(o instanceof List<?> list) {
			out.print("[");
			if(!list.isEmpty()) {
				String nextIndent = indent + this.indent;
				newline(out, nextIndent);
				boolean first = true;
				for(Object i : list) {
					if(!first) {
						out.print(",");
						newline(out, nextIndent);
					}
					first = false;
					print(out, nextIndent, i);
				}
				newline(out, indent);
			}
			out.print("]");
		}
		else if(o instanceof String s)
			out.printf("\"%s\"", s);
		else {
			// TODO check valid types, else throw exception
			out.print(o.toString());
		}
	}

	public void print(PrintStream out, Object o) {
		print(out, "", o);
	}

}
