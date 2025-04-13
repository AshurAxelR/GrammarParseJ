package com.xrbpowered.parser.examples.json;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	
	private void printMap(PrintStream out, String indent, Map<?, ?> map) {
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
				else 
					throw new UnsupportedOperationException("requires String keys");
			}
			newline(out, indent);
		}
		out.print("}");
	}
	
	private void printList(PrintStream out, String indent, Collection<?> list) {
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
	
	private void print(PrintStream out, String indent, Object o) {
		if(o==null)
			out.print("null");
		else if(o instanceof Map<?, ?> map)
			printMap(out, indent, map);
		else if(o instanceof Collection<?> col)
			printList(out, indent, col);
		else if(o instanceof Object[] array)
			printList(out, indent, Arrays.asList(array));
		else if(o.getClass().isArray()) {
			// convert primitive array
			int n = Array.getLength(o);
			ArrayList<Object> list = new ArrayList<>(n);
			for(int i=0; i<n; i++)
				list.add(Array.get(o, i));
			printList(out, indent, list);
		}
		else if(o instanceof String s)
			out.printf("\"%s\"", StringLiterals.escape(s));
		else if(o instanceof Boolean
				|| o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long
				|| o instanceof Float || o instanceof Double) {
			out.print(o.toString());
		}
		else
			throw new UnsupportedOperationException("unsuported type "+o.getClass().getTypeName());
	}

	public void print(PrintStream out, Object o) {
		print(out, "", o);
	}
	
	public void printFile(File file, Object o) {
	    try (PrintStream out = new PrintStream(file)) {
	        print(out, o);
	    }
		catch(IOException ex) {
			System.err.println(ex.getMessage());
		}
	}

	public String printString(Object o) {
	    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    		PrintStream out = new PrintStream(bytes)) {
	        print(out, o);
	        return bytes.toString();
	    }
		catch(IOException ex) {
			return null; // never happens
		}
	}

}
